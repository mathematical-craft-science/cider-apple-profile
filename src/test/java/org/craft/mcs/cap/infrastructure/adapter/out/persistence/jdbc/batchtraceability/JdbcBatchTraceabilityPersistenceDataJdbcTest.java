package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateAdvance;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateInitialization;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStatesTransition;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.RebuildCurrentMaterialStatesUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.TransitionMaterialStatesUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.application.usecase.RebuildCurrentMaterialStatesService;
import org.craft.mcs.cap.core.batchtraceability.application.usecase.TransitionMaterialStatesService;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.id;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcBatchTraceabilityStore.class,
    JdbcBatchTraceabilityQueryStore.class,
    JdbcCurrentMaterialStateStore.class,
    RebuildCurrentMaterialStatesService.class,
    TransitionMaterialStatesService.class,
    JdbcBatchTraceabilityPersistenceDataJdbcTest.TransitionTestConfiguration.class
})
class JdbcBatchTraceabilityPersistenceDataJdbcTest {

  @Autowired
  private BatchTraceabilityStore store;

  @Autowired
  private BatchTraceabilityQueryStore queryStore;

  @Autowired
  private CurrentMaterialStateStore currentStateStore;

  @Autowired
  private TransitionMaterialStatesUseCase transitionMaterialStatesUseCase;

  @Autowired
  private RebuildCurrentMaterialStatesUseCase rebuildCurrentMaterialStatesUseCase;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private PlatformTransactionManager transactionManager;

  private final WorkspaceId workspaceId = WorkspaceId.newId();

  @BeforeEach
  void createWorkspace() {
    insertWorkspace(workspaceId, "Traceability test workspace");
  }

  private void insertWorkspace(
      WorkspaceId id,
      String name
  ) {
    final var now = OffsetDateTime.parse("2026-05-18T00:00:00Z");
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
        id.value(),
        name,
        "CIDERY",
        "ACTIVE",
        0L,
        now,
        now
    );
  }

  @Test
  void isolatesSubjectQueriesByWorkspace() {
    final var otherWorkspaceId = WorkspaceId.newId();
    insertWorkspace(otherWorkspaceId, "Other traceability test workspace");
    final var sharedSubject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "shared-subject");
    final var firstStep = productionCreationStep(workspaceId, sharedSubject, "first-output");
    final var otherStep = productionCreationStep(otherWorkspaceId, sharedSubject, "other-output");

    store.append(firstStep);
    store.append(otherStep);

    assertThat(queryStore.findByInputSubject(workspaceId, sharedSubject))
        .singleElement()
        .returns(workspaceId, BatchTraceStep::workspaceId)
        .returns(firstStep.stepId(), BatchTraceStep::stepId);
  }

  @Test
  void persistsAndFindsProductionCreationStepWithSeveralInputs() {
    final var firstInput = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var secondInput = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-2");
    final var output = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var step = BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(
            BatchTraceStepSubject.of(firstInput),
            BatchTraceStepSubject.of(secondInput)
        ),
        List.of(BatchTraceStepSubject.of(output)),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    );

    store.append(step);

    final var found = queryStore.findByInputSubject(workspaceId, firstInput);

    assertThat(found).singleElement()
        .satisfies(foundStep -> {
          assertThat(foundStep.stepId()).isEqualTo(step.stepId());
          assertThat(foundStep.stepType()).isEqualTo(BatchTraceStepType.PRODUCTION_BATCH_CREATED);
          assertThat(foundStep.occurredAt()).isEqualTo(Instant.parse("2026-05-18T00:00:00Z"));
          assertThat(foundStep.inputs())
              .extracting(input -> input.subject().id())
              .containsExactly(firstInput.id(), secondInput.id());
          assertThat(foundStep.outputs())
              .extracting(foundOutput -> foundOutput.subject().id())
              .containsExactly(output.id());
        });
  }

  @Test
  void persistsAndFindsStepByOutputSubject() {
    final var output = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var step = BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(BatchTraceStepSubject.of(subject(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        List.of(BatchTraceStepSubject.of(output)),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    );

    store.append(step);

    final var found = queryStore.findByOutputSubject(workspaceId, output);

    assertThat(found).singleElement()
        .satisfies(foundStep -> {
          assertThat(foundStep.stepId()).isEqualTo(step.stepId());
          assertThat(foundStep.occurredAt()).isEqualTo(Instant.parse("2026-05-18T00:00:00Z"));
          assertThat(foundStep.inputs())
              .extracting(input -> input.subject().id())
              .containsExactly(id("feedstock-1"));
          assertThat(foundStep.outputs())
              .extracting(foundOutput -> foundOutput.subject().id())
              .containsExactly(id("production-1"));
        });
  }

  @Test
  void rejectsOutputStateAlreadyDefinedByAnotherTraceStep() {
    final var sharedStateId = MaterialStateId.newId();
    final var firstOutput = state(
        sharedStateId,
        subject(BatchTraceSubjectType.PRODUCTION_BATCH, "unique-state-production-1"),
        "50"
    );
    final var secondOutput = state(
        sharedStateId,
        subject(BatchTraceSubjectType.PRODUCTION_BATCH, "unique-state-production-2"),
        "50"
    );
    final var firstStep = productionCreationStep(firstOutput, "unique-state-feedstock-1");
    final var secondStep = productionCreationStep(secondOutput, "unique-state-feedstock-2");

    store.append(firstStep);

    assertThatThrownBy(() -> store.append(secondStep))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessageContaining("batch trace state");
  }

  @Test
  void findsCurrentStateUnderConsistencyGuard() {
    final var subject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-current");
    final var initialStateId = BatchTraceStepSubject.of(subject).traceStateId();
    currentStateStore.initialize(workspaceId, subject, initialStateId);

    assertThat(currentStateStore.lockCurrent(subject))
        .get()
        .returns(workspaceId, CurrentMaterialState::workspaceId)
        .returns(subject, CurrentMaterialState::subject)
        .returns(initialStateId, CurrentMaterialState::materialStateId);
  }

  @Test
  void advancesSeveralSourcesAndInitializesProductionInOneTraceStep() {
    final var firstSource = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "transition-source-1");
    final var secondSource = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "transition-source-2");
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "transition-production");
    final var firstExpected = MaterialStateId.newId();
    final var secondExpected = MaterialStateId.newId();
    final var firstNext = MaterialStateId.newId();
    final var secondNext = MaterialStateId.newId();
    final var productionInitial = MaterialStateId.newId();
    currentStateStore.initialize(workspaceId, firstSource, firstExpected);
    currentStateStore.initialize(workspaceId, secondSource, secondExpected);

    transitionMaterialStatesUseCase.transition(transition(
        List.of(
            advance(firstSource, firstExpected, firstNext, "100", "40"),
            advance(secondSource, secondExpected, secondNext, "120", "80")
        ),
        initialization(production, productionInitial, "100")
    ));

    assertThat(currentStateStore.findCurrent(firstSource))
        .get()
        .returns(firstNext, CurrentMaterialState::materialStateId)
        .returns(1L, CurrentMaterialState::version);
    assertThat(currentStateStore.findCurrent(secondSource))
        .get()
        .returns(secondNext, CurrentMaterialState::materialStateId)
        .returns(1L, CurrentMaterialState::version);
    assertThat(currentStateStore.findCurrent(production))
        .get()
        .returns(productionInitial, CurrentMaterialState::materialStateId)
        .returns(0L, CurrentMaterialState::version);
    assertThat(currentStateStore.findState(firstNext))
        .get()
        .satisfies(state -> assertThat(state.state().snapshot().volume().liters())
            .isEqualByComparingTo("40"));
    assertThat(queryStore.findByOutputSubject(workspaceId, production))
        .singleElement()
        .satisfies(step -> assertThat(step.outputs())
            .hasSize(3));
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void rebuildsCurrentPointersFromImmutableTrace() {
    final var otherWorkspaceId = WorkspaceId.newId();
    insertWorkspace(otherWorkspaceId, "Other rebuild workspace");
    final var feedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "rebuild-feedstock");
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "rebuild-production");
    final var otherFeedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "other-rebuild-feedstock");
    final var initialFeedstockState = MaterialStateId.newId();
    final var remainingFeedstockState = MaterialStateId.newId();
    final var productionState = MaterialStateId.newId();
    final var otherFeedstockState = MaterialStateId.newId();
    final var creationStep = BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
        List.of(),
        List.of(state(initialFeedstockState, feedstock, "100")),
        Instant.parse("2026-05-18T09:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    );
    final var materialTransition = transition(
        List.of(advance(
            feedstock,
            initialFeedstockState,
            remainingFeedstockState,
            "100",
            "40"
        )),
        initialization(production, productionState, "60")
    );
    store.append(creationStep);
    store.append(materialTransition.traceStep());
    currentStateStore.initialize(workspaceId, feedstock, initialFeedstockState);
    currentStateStore.compareAndSet(
        workspaceId,
        feedstock,
        initialFeedstockState,
        remainingFeedstockState
    );
    currentStateStore.initialize(workspaceId, production, productionState);
    currentStateStore.initialize(otherWorkspaceId, otherFeedstock, otherFeedstockState);
    jdbcTemplate.update(
        "DELETE FROM batch_trace_current_states WHERE workspace_id = ?",
        workspaceId.value()
    );

    final var result = rebuildCurrentMaterialStatesUseCase.rebuild(workspaceId);

    assertThat(result.rebuiltSubjectCount()).isEqualTo(2);
    assertThat(currentStateStore.findCurrent(feedstock))
        .get()
        .returns(remainingFeedstockState, CurrentMaterialState::materialStateId)
        .returns(1L, CurrentMaterialState::version);
    assertThat(currentStateStore.findCurrent(production))
        .get()
        .returns(productionState, CurrentMaterialState::materialStateId)
        .returns(0L, CurrentMaterialState::version);
    assertThat(currentStateStore.findCurrent(otherFeedstock))
        .get()
        .returns(otherWorkspaceId, CurrentMaterialState::workspaceId)
        .returns(otherFeedstockState, CurrentMaterialState::materialStateId);
    assertThat(currentStateStore.findState(remainingFeedstockState))
        .get()
        .satisfies(state -> assertThat(state.state().snapshot().volume().liters())
            .isEqualByComparingTo("40"));
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void rebuildDoesNotWaitForAnUnchangedPointerLock() throws Exception {
    final var feedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "unchanged-rebuild-feedstock");
    final var initialState = MaterialStateId.newId();
    store.append(BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
        List.of(),
        List.of(state(initialState, feedstock, "100")),
        Instant.parse("2026-05-18T09:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    ));
    currentStateStore.initialize(workspaceId, feedstock, initialState);
    final var pointerLocked = new CountDownLatch(1);
    final var releasePointer = new CountDownLatch(1);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var pointerLock = executor.submit(() ->
          new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            currentStateStore.lockCurrent(feedstock).orElseThrow();
            pointerLocked.countDown();
            await(releasePointer);
          })
      );
      assertThat(pointerLocked.await(2, TimeUnit.SECONDS)).isTrue();

      final var rebuild = executor.submit(() -> rebuildCurrentMaterialStatesUseCase.rebuild(workspaceId));

      try {
        assertThat(rebuild.get(2, TimeUnit.SECONDS).rebuiltSubjectCount()).isEqualTo(1);
      } finally {
        releasePointer.countDown();
      }
      pointerLock.get(2, TimeUnit.SECONDS);
    }
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void repeatableReadRebuildDoesNotOverwriteAConcurrentPointerAdvance() throws Exception {
    final var feedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "concurrent-rebuild-feedstock");
    final var initialState = MaterialStateId.newId();
    final var advancedState = MaterialStateId.newId();
    currentStateStore.initialize(workspaceId, feedstock, initialState);
    final var snapshotRead = new CountDownLatch(1);
    final var pointerAdvanced = new CountDownLatch(1);
    final var rebuildTransaction = new TransactionTemplate(transactionManager);
    rebuildTransaction.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var rebuild = executor.submit(() -> rebuildTransaction.executeWithoutResult(status -> {
        final var stateFromSnapshot = currentStateStore.findCurrent(feedstock).orElseThrow();
        snapshotRead.countDown();
        await(pointerAdvanced);
        currentStateStore.replaceAll(workspaceId, List.of(stateFromSnapshot));
      }));
      assertThat(snapshotRead.await(2, TimeUnit.SECONDS)).isTrue();

      try {
        currentStateStore.compareAndSet(workspaceId, feedstock, initialState, advancedState);
      } finally {
        pointerAdvanced.countDown();
      }
      rebuild.get(2, TimeUnit.SECONDS);
    }

    assertThat(currentStateStore.findCurrent(feedstock))
        .get()
        .returns(advancedState, CurrentMaterialState::materialStateId)
        .returns(1L, CurrentMaterialState::version);
  }

  @Test
  void rejectsStaleExpectedStateWithoutAppendingTrace() {
    final var source = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "stale-transition-source");
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "stale-transition-output");
    final var actual = MaterialStateId.newId();
    final var stale = MaterialStateId.newId();
    final var next = MaterialStateId.newId();
    currentStateStore.initialize(workspaceId, source, actual);

    assertThatThrownBy(() -> transitionMaterialStatesUseCase.transition(transition(
        List.of(advance(source, stale, next, "100", "40")),
        initialization(production, MaterialStateId.newId(), "60")
    )))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessageContaining("current material state");
    assertThat(currentStateStore.findCurrent(source))
        .get()
        .returns(actual, CurrentMaterialState::materialStateId)
        .returns(0L, CurrentMaterialState::version);
    assertThat(queryStore.findByOutputSubject(workspaceId, production))
        .isEmpty();
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void staleSourceRollsBackEarlierPointerAdvanceInMultiSourceTransition() {
    final var firstSource = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "atomic-source-1");
    final var secondSource = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "atomic-source-2");
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "atomic-production");
    final var firstExpected = MaterialStateId.newId();
    final var secondActual = MaterialStateId.newId();
    currentStateStore.initialize(workspaceId, firstSource, firstExpected);
    currentStateStore.initialize(workspaceId, secondSource, secondActual);
    final var materialTransition = transition(
        List.of(
            advance(firstSource, firstExpected, MaterialStateId.newId(), "100", "40"),
            advance(secondSource, MaterialStateId.newId(), MaterialStateId.newId(), "120", "80")
        ),
        initialization(production, MaterialStateId.newId(), "100")
    );

    assertThatThrownBy(() -> new TransactionTemplate(transactionManager)
        .executeWithoutResult(status -> transitionMaterialStatesUseCase.transition(materialTransition)))
        .isInstanceOf(ConcurrentResourceModificationException.class);
    assertThat(currentStateStore.findCurrent(firstSource))
        .get()
        .returns(firstExpected, CurrentMaterialState::materialStateId)
        .returns(0L, CurrentMaterialState::version);
    assertThat(currentStateStore.findCurrent(secondSource))
        .get()
        .returns(secondActual, CurrentMaterialState::materialStateId)
        .returns(0L, CurrentMaterialState::version);
    assertThat(currentStateStore.findCurrent(production))
        .isEmpty();
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void rollsBackPointerInitializationAndTraceTogether() {
    final var source = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "rollback-transition-source");
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "rollback-transition-output");
    final var expected = MaterialStateId.newId();
    final var next = MaterialStateId.newId();
    currentStateStore.initialize(workspaceId, source, expected);
    final var materialTransition = transition(
        List.of(advance(source, expected, next, "100", "40")),
        initialization(production, MaterialStateId.newId(), "60")
    );

    assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
      transitionMaterialStatesUseCase.transition(materialTransition);
      throw new IllegalStateException("force rollback");
    }))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("force rollback");
    assertThat(currentStateStore.findCurrent(source))
        .get()
        .returns(expected, CurrentMaterialState::materialStateId)
        .returns(0L, CurrentMaterialState::version);
    assertThat(currentStateStore.findCurrent(production))
        .isEmpty();
    assertThat(queryStore.findByOutputSubject(workspaceId, production))
        .isEmpty();
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void transitionsOnIndependentSubjectsDoNotBlockEachOther() throws Exception {
    final var firstSource = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "cas-lock-source-1");
    final var secondSource = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "cas-lock-source-2");
    final var firstExpected = MaterialStateId.newId();
    final var secondExpected = MaterialStateId.newId();
    currentStateStore.initialize(workspaceId, firstSource, firstExpected);
    currentStateStore.initialize(workspaceId, secondSource, secondExpected);
    final var firstTransitionApplied = new CountDownLatch(1);
    final var releaseFirstTransition = new CountDownLatch(1);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var firstTransition = executor.submit(() ->
          new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            transitionMaterialStatesUseCase.transition(transition(
                List.of(advance(firstSource, firstExpected, MaterialStateId.newId(), "100", "40")),
                initialization(
                    subject(BatchTraceSubjectType.PRODUCTION_BATCH, "cas-lock-output-1"),
                    MaterialStateId.newId(),
                    "60"
                )
            ));
            firstTransitionApplied.countDown();
            await(releaseFirstTransition);
          })
      );
      assertThat(firstTransitionApplied.await(2, TimeUnit.SECONDS)).isTrue();

      final var secondTransition = executor.submit(() ->
          new TransactionTemplate(transactionManager).executeWithoutResult(status ->
              transitionMaterialStatesUseCase.transition(transition(
                  List.of(advance(
                      secondSource,
                      secondExpected,
                      MaterialStateId.newId(),
                      "100",
                      "50"
                  )),
                  initialization(
                      subject(BatchTraceSubjectType.PRODUCTION_BATCH, "cas-lock-output-2"),
                      MaterialStateId.newId(),
                      "50"
                  )
              ))
          )
      );

      try {
        secondTransition.get(2, TimeUnit.SECONDS);
      } finally {
        releaseFirstTransition.countDown();
      }
      firstTransition.get(2, TimeUnit.SECONDS);
    }
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void currentStateLockLocksOnlyRequestedSubject() throws Exception {
    final var firstSubject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "first-lock-subject");
    final var secondSubject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "second-lock-subject");
    currentStateStore.initialize(workspaceId, firstSubject, BatchTraceStepSubject.of(firstSubject).traceStateId());
    currentStateStore.initialize(workspaceId, secondSubject, BatchTraceStepSubject.of(secondSubject).traceStateId());
    final var firstSubjectLocked = new CountDownLatch(1);
    final var releaseFirstSubject = new CountDownLatch(1);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var firstLock = executor.submit(() -> new TransactionTemplate(transactionManager).executeWithoutResult(
          status -> {
            currentStateStore.lockCurrent(firstSubject).orElseThrow();
            firstSubjectLocked.countDown();
            await(releaseFirstSubject);
          }
      ));
      assertThat(firstSubjectLocked.await(2, TimeUnit.SECONDS)).isTrue();

      final var secondLock = executor.submit(() -> new TransactionTemplate(transactionManager).execute(
          status -> currentStateStore.lockCurrent(secondSubject).orElseThrow()
      ));

      try {
        assertThat(secondLock.get(2, TimeUnit.SECONDS).subject()).isEqualTo(secondSubject);
      } finally {
        releaseFirstSubject.countDown();
      }
      firstLock.get(2, TimeUnit.SECONDS);
    }
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void currentStateLockSerializesWritesForTheSameSubject() throws Exception {
    final var subject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "exclusive-lock-subject");
    currentStateStore.initialize(workspaceId, subject, BatchTraceStepSubject.of(subject).traceStateId());
    final var subjectLocked = new CountDownLatch(1);
    final var releaseSubject = new CountDownLatch(1);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var firstLock = executor.submit(() -> new TransactionTemplate(transactionManager).executeWithoutResult(
          status -> {
            currentStateStore.lockCurrent(subject).orElseThrow();
            subjectLocked.countDown();
            await(releaseSubject);
          }
      ));
      assertThat(subjectLocked.await(2, TimeUnit.SECONDS)).isTrue();

      final var secondLock = executor.submit(() -> new TransactionTemplate(transactionManager).execute(
          status -> currentStateStore.lockCurrent(subject).orElseThrow()
      ));

      try {
        assertThatThrownBy(() -> secondLock.get(250, TimeUnit.MILLISECONDS))
            .isInstanceOf(TimeoutException.class);
      } finally {
        releaseSubject.countDown();
      }
      firstLock.get(2, TimeUnit.SECONDS);
      assertThat(secondLock.get(2, TimeUnit.SECONDS).subject()).isEqualTo(subject);
    }
  }

  private static void await(CountDownLatch latch) {
    try {
      if (!latch.await(5, TimeUnit.SECONDS)) {
        throw new AssertionError("timed out while holding the material subject lock");
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AssertionError("interrupted while holding the material subject lock", exception);
    }
  }

  private MaterialStatesTransition transition(
      List<StateAdvanceFixture> advances,
      StateInitializationFixture initialization
  ) {
    final var step = BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        advances.stream()
            .map(StateAdvanceFixture::input)
            .toList(),
        Stream.concat(
            advances.stream().map(StateAdvanceFixture::output),
            Stream.of(initialization.output())
        ).toList(),
        Instant.parse("2026-05-18T10:15:30Z"),
        BatchTraceActorReference.system("test"),
        null
    );
    return new MaterialStatesTransition(
        step,
        advances.stream()
            .map(StateAdvanceFixture::advance)
            .toList(),
        List.of(initialization.initialization())
    );
  }

  private static StateAdvanceFixture advance(
      BatchTraceSubject subject,
      MaterialStateId expectedStateId,
      MaterialStateId nextStateId,
      String volumeBefore,
      String remainingVolume
  ) {
    return new StateAdvanceFixture(
        new MaterialStateAdvance(subject, expectedStateId, nextStateId),
        state(expectedStateId, subject, volumeBefore),
        state(nextStateId, subject, remainingVolume)
    );
  }

  private static StateInitializationFixture initialization(
      BatchTraceSubject subject,
      MaterialStateId stateId,
      String volume
  ) {
    return new StateInitializationFixture(
        new MaterialStateInitialization(subject, stateId),
        state(stateId, subject, volume)
    );
  }

  private static BatchTraceStepSubject state(
      MaterialStateId stateId,
      BatchTraceSubject subject,
      String volume
  ) {
    return new BatchTraceStepSubject(
        stateId,
        subject,
        BatchTraceMaterialSnapshot.ofVolume(VolumeValue.ofLiters(volume))
    );
  }

  private static BatchTraceStep productionCreationStep(
      WorkspaceId workspaceId,
      BatchTraceSubject input,
      String outputId
  ) {
    return BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(BatchTraceStepSubject.of(input)),
        List.of(BatchTraceStepSubject.of(subject(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            outputId
        ))),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    );
  }

  private BatchTraceStep productionCreationStep(
      BatchTraceStepSubject output,
      String inputId
  ) {
    return BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(BatchTraceStepSubject.of(subject(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            inputId
        ))),
        List.of(output),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    );
  }

  private record StateAdvanceFixture(
      MaterialStateAdvance advance,
      BatchTraceStepSubject input,
      BatchTraceStepSubject output
  ) {
  }

  private record StateInitializationFixture(
      MaterialStateInitialization initialization,
      BatchTraceStepSubject output
  ) {
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class TransitionTestConfiguration {

    @Bean
    BatchTraceabilityAccessControl batchTraceabilityAccessControl() {
      return new BatchTraceabilityAccessControl() {
        @Override
        public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
        }

        @Override
        public void requireWriteAccess(
            @NonNull WorkspaceId workspaceId,
            @NonNull Set<BatchTraceSubjectType> subjectTypes
        ) {
        }
      };
    }
  }
}
