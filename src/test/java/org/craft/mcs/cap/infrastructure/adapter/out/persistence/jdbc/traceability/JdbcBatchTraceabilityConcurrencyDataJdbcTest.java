package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.id;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcBatchTraceabilityStore.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JdbcBatchTraceabilityConcurrencyDataJdbcTest {

  @Autowired
  private BatchTraceabilityStore store;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private JdbcTemplate jdbc;

  private WorkspaceId workspaceId;

  @BeforeEach
  void createWorkspace() {
    workspaceId = WorkspaceId.newId();
    jdbc.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, 'Trace concurrency', 'CIDERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value()
    );
  }

  @AfterEach
  void deleteWorkspace() {
    jdbc.update("DELETE FROM batch_trace_steps WHERE workspace_id = ?", workspaceId.value());
    jdbc.update("DELETE FROM workspaces WHERE workspace_id = ?", workspaceId.value());
  }

  @Test
  void duplicateOutputStateRollsBackTheWholeLosingStep() {
    final var stateId = MaterialStateId.newId();
    final var winning = creationStep("winning", stateId);
    final var losing = creationStep("losing", stateId);
    inTransaction(() -> store.append(winning));

    assertThatThrownBy(() -> inTransaction(() -> store.append(losing)))
        .isInstanceOf(ConcurrentResourceModificationException.class);
    assertThat(stepCount(losing)).isZero();
    assertThat(stepCount(winning)).isOne();
  }

  @Test
  void concurrentStepsCanProduceOneImmutableStateOnlyOnce() throws Exception {
    final var stateId = MaterialStateId.newId();
    final var firstStep = creationStep("first", stateId);
    final var secondStep = creationStep("second", stateId);
    final var start = new CountDownLatch(1);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var first = executor.submit(() -> appendAfter(start, firstStep));
      final var second = executor.submit(() -> appendAfter(start, secondStep));
      start.countDown();
      final var attempts = List.of(
          first.get(10, TimeUnit.SECONDS),
          second.get(10, TimeUnit.SECONDS)
      );

      assertThat(attempts)
          .filteredOn(Attempt::succeeded)
          .hasSize(1);
      assertThat(attempts)
          .filteredOn(attempt -> attempt.failure() instanceof ConcurrentResourceModificationException)
          .hasSize(1);
      assertThat(stepCount(firstStep) + stepCount(secondStep)).isOne();
      assertThat(outputStateCount(stateId)).isOne();
    }
  }

  private Attempt appendAfter(
      CountDownLatch start,
      BatchTraceStep step
  ) {
    await(start);
    try {
      inTransaction(() -> store.append(step));
      return Attempt.success();
    } catch (RuntimeException exception) {
      return Attempt.failure(exception);
    }
  }

  private void inTransaction(Runnable action) {
    new TransactionTemplate(transactionManager).executeWithoutResult(_ -> action.run());
  }

  private long stepCount(BatchTraceStep step) {
    return requireNonNull(jdbc.queryForObject(
        "SELECT COUNT(*) FROM batch_trace_steps WHERE step_id = ?",
        Long.class,
        step.stepId().value()
    ));
  }

  private long outputStateCount(MaterialStateId stateId) {
    return requireNonNull(jdbc.queryForObject(
        """
            SELECT COUNT(*)
              FROM batch_trace_step_subjects
             WHERE material_state_id = ?
               AND subject_role = 'OUTPUT'
            """,
        Long.class,
        stateId.value()
    ));
  }

  private BatchTraceStep creationStep(
      String subjectName,
      MaterialStateId stateId
  ) {
    return BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
        List.of(),
        List.of(new BatchTraceStepSubject(
            stateId,
            subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, subjectName),
            BatchTraceMaterialSnapshot.ofVolume(VolumeValue.ofLiters("100"))
        )),
        Instant.parse("2026-08-10T08:00:00Z"),
        ActorReference.machineIdentity(id("trace-concurrency-test")),
        null
    );
  }

  private static void await(CountDownLatch latch) {
    try {
      if (!latch.await(10, TimeUnit.SECONDS)) {
        throw new AssertionError("timed out while waiting for trace concurrency test coordination");
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AssertionError("trace concurrency test was interrupted", exception);
    }
  }

  private record Attempt(
      boolean succeeded,
      @Nullable RuntimeException failure
  ) {

    private static Attempt success() {
      return new Attempt(true, null);
    }

    private static Attempt failure(RuntimeException exception) {
      return new Attempt(false, requireNonNull(exception));
    }
  }
}
