package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.id;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcBatchTraceabilityStore.class,
    JdbcBatchTraceabilityQueryStore.class,
    JdbcCurrentMaterialStateStore.class
})
class JdbcBatchTraceabilityPersistenceDataJdbcTest {

  @Autowired
  private BatchTraceabilityStore store;

  @Autowired
  private BatchTraceabilityQueryStore queryStore;

  @Autowired
  private CurrentMaterialStateStore currentStateStore;

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
  void findsCurrentStateUnderConsistencyGuard() {
    final var subject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-current");
    final var initialStateId = BatchTraceStepSubject.of(subject).traceStateId();
    currentStateStore.initialize(workspaceId, subject, initialStateId);

    assertThat(currentStateStore.findCurrentForConsistency(subject))
        .get()
        .returns(workspaceId, CurrentMaterialState::workspaceId)
        .returns(subject, CurrentMaterialState::subject)
        .returns(initialStateId, CurrentMaterialState::materialStateId);
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void consistencyGuardLocksOnlyRequestedSubject() throws Exception {
    final var firstSubject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "first-lock-subject");
    final var secondSubject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "second-lock-subject");
    currentStateStore.initialize(workspaceId, firstSubject, BatchTraceStepSubject.of(firstSubject).traceStateId());
    currentStateStore.initialize(workspaceId, secondSubject, BatchTraceStepSubject.of(secondSubject).traceStateId());
    final var firstSubjectLocked = new CountDownLatch(1);
    final var releaseFirstSubject = new CountDownLatch(1);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var firstLock = executor.submit(() -> new TransactionTemplate(transactionManager).executeWithoutResult(
          status -> {
            currentStateStore.findCurrentForConsistency(firstSubject).orElseThrow();
            firstSubjectLocked.countDown();
            await(releaseFirstSubject);
          }
      ));
      assertThat(firstSubjectLocked.await(2, TimeUnit.SECONDS)).isTrue();

      final var secondLock = executor.submit(() -> new TransactionTemplate(transactionManager).execute(
          status -> currentStateStore.findCurrentForConsistency(secondSubject).orElseThrow()
      ));

      try {
        assertThat(secondLock.get(2, TimeUnit.SECONDS).subject()).isEqualTo(secondSubject);
      } finally {
        releaseFirstSubject.countDown();
      }
      firstLock.get(2, TimeUnit.SECONDS);
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
}
