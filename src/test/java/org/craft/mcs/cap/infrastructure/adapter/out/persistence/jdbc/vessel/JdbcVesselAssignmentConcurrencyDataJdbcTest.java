package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.craft.mcs.cap.core.vessel.application.VesselOccupiedException;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.port.in.ValidateVesselAssignmentCommand;
import org.craft.mcs.cap.core.vessel.application.port.in.ValidateVesselAssignmentUseCase;
import org.craft.mcs.cap.core.vessel.application.usecase.ValidateVesselAssignmentService;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcVesselRepository.class,
    JdbcVesselCurrentContentQuery.class,
    ValidateVesselAssignmentService.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JdbcVesselAssignmentConcurrencyDataJdbcTest {

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private JdbcTemplate jdbc;

  @Autowired
  private ValidateVesselAssignmentUseCase assignmentService;

  @MockitoBean
  private VesselAccessControl vesselAccessControl;

  @Test
  void rejectsAssignmentValidationWithoutCallerTransaction() {
    final var workspaceId = persistWorkspace("Missing transaction cidery");
    final var vesselId = persistVessel(workspaceId, "Tank M-01");

    assertThatThrownBy(() -> assignmentService.validate(assignmentCommand(workspaceId, vesselId)))
        .isInstanceOf(IllegalTransactionStateException.class);
  }

  @Test
  void serializesAssignmentsToOneVesselUntilTheCreatingTransactionCommits() throws Exception {
    final var workspaceId = persistWorkspace("Concurrent assignment cidery");
    final var vesselId = persistVessel(workspaceId, "Tank C-01");
    final var firstValidated = new CountDownLatch(1);
    final var allowFirstCommit = new CountDownLatch(1);
    final var secondAttemptingLock = new CountDownLatch(1);
    final var command = assignmentCommand(workspaceId, vesselId);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var first = executor.submit(() -> transactionTemplate().executeWithoutResult(_ -> {
        assignmentService.validate(command);
        firstValidated.countDown();
        await(allowFirstCommit);
        persistFeedstock(workspaceId, vesselId, "First occupant");
      }));
      assertThat(firstValidated.await(5, TimeUnit.SECONDS)).isTrue();

      final var second = executor.submit(() -> transactionTemplate().execute(_ -> {
        secondAttemptingLock.countDown();
        return assignmentService.validate(command);
      }));
      assertThat(secondAttemptingLock.await(5, TimeUnit.SECONDS)).isTrue();
      assertThat(second).isNotDone();

      allowFirstCommit.countDown();
      first.get(5, TimeUnit.SECONDS);
      assertThatThrownBy(() -> second.get(5, TimeUnit.SECONDS))
          .isInstanceOf(ExecutionException.class)
          .cause()
          .isInstanceOf(VesselOccupiedException.class);
    } finally {
      allowFirstCommit.countDown();
    }
  }

  @Test
  void lockingOneVesselDoesNotBlockAssignmentValidationForAnotherVessel() throws Exception {
    final var workspaceId = persistWorkspace("Independent vessels cidery");
    final var firstVesselId = persistVessel(workspaceId, "Tank I-01");
    final var secondVesselId = persistVessel(workspaceId, "Tank I-02");
    final var firstValidated = new CountDownLatch(1);
    final var releaseFirst = new CountDownLatch(1);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var first = executor.submit(() -> transactionTemplate().executeWithoutResult(_ -> {
        assignmentService.validate(assignmentCommand(workspaceId, firstVesselId));
        firstValidated.countDown();
        await(releaseFirst);
      }));
      assertThat(firstValidated.await(5, TimeUnit.SECONDS)).isTrue();

      final var second = executor.submit(() -> transactionTemplate().execute(
          _ -> assignmentService.validate(assignmentCommand(workspaceId, secondVesselId))
      ));

      assertThat(second.get(5, TimeUnit.SECONDS).vesselId()).isEqualTo(secondVesselId);
      releaseFirst.countDown();
      first.get(5, TimeUnit.SECONDS);
    } finally {
      releaseFirst.countDown();
    }
  }

  private static ValidateVesselAssignmentCommand assignmentCommand(
      WorkspaceId workspaceId,
      VesselId vesselId
  ) {
    return new ValidateVesselAssignmentCommand(vesselId, workspaceId, VolumeValue.ofLiters("100"));
  }

  private TransactionTemplate transactionTemplate() {
    return new TransactionTemplate(transactionManager);
  }

  private WorkspaceId persistWorkspace(String name) {
    final var workspaceId = WorkspaceId.newId();
    jdbc.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, ?, 'CIDERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value(),
        name
    );
    return workspaceId;
  }

  private VesselId persistVessel(
      WorkspaceId workspaceId,
      String name
  ) {
    final var vesselId = VesselId.newId();
    jdbc.update(
        """
            INSERT INTO vessels (
                vessel_id, workspace_id, name, nominal_capacity_liters, material,
                operational_status, version, created_at, updated_at
            ) VALUES (?, ?, ?, 500, 'STAINLESS_STEEL', 'OPERATIONAL', 0,
                      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        vesselId.value(),
        workspaceId.value(),
        name
    );
    return vesselId;
  }

  private void persistFeedstock(
      WorkspaceId workspaceId,
      VesselId vesselId,
      String name
  ) {
    jdbc.update(
        """
            INSERT INTO feedstock_batches (
                batch_id, workspace_id, name, vessel_id, type, season_starts_on, season_ends_on,
                lifecycle, available_volume_liters, current_material_state_id, version, created_at, updated_at
            ) VALUES (?, ?, ?, ?, 'JUICE', DATE '2026-01-01', DATE '2026-12-31',
                      'ACTIVE', 100, ?, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        newUuid(),
        workspaceId.value(),
        name,
        vesselId.value(),
        MaterialStateId.newId().value()
    );
  }

  private static void await(CountDownLatch latch) {
    try {
      if (!latch.await(5, TimeUnit.SECONDS)) {
        throw new AssertionError("timed out while waiting for concurrent test coordination");
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AssertionError("concurrent test was interrupted", exception);
    }
  }
}
