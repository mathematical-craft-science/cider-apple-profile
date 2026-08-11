package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.CreateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchTraceRecorder;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.CreateFeedstockBatchService;
import org.craft.mcs.cap.core.feedstock.application.vessel.port.out.FeedstockVesselAssignmentValidator;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.vessel.application.VesselOccupiedException;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCurrentContentQuery;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselRepository;
import org.craft.mcs.cap.core.vessel.application.usecase.ValidateVesselAssignmentService;
import org.craft.mcs.cap.infrastructure.adapter.out.vessel.VesselAssignmentValidationAdapter;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcFeedstockBatchRepository.class,
    CreateFeedstockBatchVesselConcurrencyDataJdbcTest.VesselPersistenceConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CreateFeedstockBatchVesselConcurrencyDataJdbcTest {

  @Autowired
  private JdbcFeedstockBatchRepository feedstockBatchRepository;

  @Autowired
  private VesselRepository vesselRepository;

  @Autowired
  private VesselCurrentContentQuery vesselCurrentContentQuery;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private JdbcTemplate jdbc;

  @Test
  void completeCreateUseCaseSerializesCompetingAssignmentsToOneVessel() throws Exception {
    final var workspaceId = persistWorkspace();
    final var vesselId = persistVessel(workspaceId);
    final var firstValidated = new CountDownLatch(1);
    final var releaseFirst = new CountDownLatch(1);
    final var secondAttemptingValidation = new CountDownLatch(1);
    final var attempts = new AtomicInteger();
    final var validator = coordinatedValidator(
        firstValidated,
        releaseFirst,
        secondAttemptingValidation,
        attempts
    );
    final var service = createService(validator);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var first = executor.submit(() -> service.create(command(workspaceId, vesselId, "First batch")));
      assertThat(firstValidated.await(5, TimeUnit.SECONDS)).isTrue();

      final var second = executor.submit(() -> service.create(command(workspaceId, vesselId, "Second batch")));
      assertThat(secondAttemptingValidation.await(5, TimeUnit.SECONDS)).isTrue();
      assertThat(second).isNotDone();

      releaseFirst.countDown();
      assertThat(first.get(5, TimeUnit.SECONDS).batch().batch().name())
          .isEqualTo(BatchName.of("First batch"));
      assertThatThrownBy(() -> second.get(5, TimeUnit.SECONDS))
          .isInstanceOf(ExecutionException.class)
          .cause()
          .isInstanceOf(VesselOccupiedException.class);
      assertThat(jdbc.queryForObject(
          "SELECT COUNT(*) FROM feedstock_batches WHERE vessel_id = ? AND deleted_at IS NULL",
          Long.class,
          vesselId.value()
      )).isEqualTo(1L);
    } finally {
      releaseFirst.countDown();
    }
  }

  @Test
  void traceFailureRollsBackFeedstockCreationAndLeavesVesselAvailable() {
    final var workspaceId = persistWorkspace();
    final var vesselId = persistVessel(workspaceId);
    final var traceRecorder = mock(FeedstockBatchTraceRecorder.class);
    doThrow(new IllegalStateException("trace persistence failed"))
        .when(traceRecorder)
        .recordCreated(any(), any());
    final var service = createService(assignmentValidator(), traceRecorder);

    assertThatThrownBy(() -> service.create(command(workspaceId, vesselId, "Rolled back batch")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("trace persistence failed");
    assertThat(jdbc.queryForObject(
        "SELECT COUNT(*) FROM feedstock_batches WHERE vessel_id = ? AND deleted_at IS NULL",
        Long.class,
        vesselId.value()
    )).isZero();
    assertThat(vesselCurrentContentQuery.findByVesselId(vesselId)).isEmpty();
  }

  private @NonNull FeedstockVesselAssignmentValidator coordinatedValidator(
      @NonNull CountDownLatch firstValidated,
      @NonNull CountDownLatch releaseFirst,
      @NonNull CountDownLatch secondAttemptingValidation,
      @NonNull AtomicInteger attempts
  ) {
    final var delegate = assignmentValidator();

    return (workspaceId, vesselId, materialVolume) -> {
      final var attempt = attempts.incrementAndGet();
      if (attempt == 2) {
        secondAttemptingValidation.countDown();
      }
      final var reference = delegate.requireAssignable(workspaceId, vesselId, materialVolume);
      if (attempt == 1) {
        firstValidated.countDown();
        await(releaseFirst);
      }
      return reference;
    };
  }

  private @NonNull CreateFeedstockBatchService createService(
      @NonNull FeedstockVesselAssignmentValidator validator
  ) {
    return createService(validator, mock(FeedstockBatchTraceRecorder.class));
  }

  private @NonNull CreateFeedstockBatchService createService(
      @NonNull FeedstockVesselAssignmentValidator validator,
      @NonNull FeedstockBatchTraceRecorder traceRecorder
  ) {
    return new CreateFeedstockBatchService(
        feedstockBatchRepository,
        traceRecorder,
        mock(FeedstockAccessControl.class),
        validator,
        transactionHelper(),
        Clock.fixed(Instant.parse("2026-08-09T12:00:00Z"), ZoneOffset.UTC)
    );
  }

  private @NonNull FeedstockVesselAssignmentValidator assignmentValidator() {
    return new VesselAssignmentValidationAdapter(new ValidateVesselAssignmentService(
        vesselRepository,
        vesselCurrentContentQuery,
        mock(VesselAccessControl.class)
    ));
  }

  private @NonNull TransactionHelper transactionHelper() {
    final var transactions = new TransactionTemplate(transactionManager);
    return new TransactionHelper() {
      @Override
      public <T> @NonNull T callInTransactionNonNull(@NonNull Supplier<T> supplier) {
        return requireNonNull(transactions.execute(_ -> supplier.get()));
      }

      @Override
      public void executeInTransaction(@NonNull Runnable runnable) {
        transactions.executeWithoutResult(_ -> runnable.run());
      }
    };
  }

  private static @NonNull CreateFeedstockBatchCommand command(
      @NonNull WorkspaceId workspaceId,
      @NonNull VesselId vesselId,
      @NonNull String name
  ) {
    return new CreateFeedstockBatchCommand(
        workspaceId,
        BatchName.of(name),
        vesselId,
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-08-01"), LocalDate.parse("2026-12-31")),
        null,
        VolumeValue.ofLiters("100"),
        null
    );
  }

  private @NonNull WorkspaceId persistWorkspace() {
    final var workspaceId = WorkspaceId.newId();
    jdbc.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, 'Concurrent create cidery', 'CIDERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value()
    );
    return workspaceId;
  }

  private @NonNull VesselId persistVessel(@NonNull WorkspaceId workspaceId) {
    final var vesselId = VesselId.newId();
    jdbc.update(
        """
            INSERT INTO vessels (
                vessel_id, workspace_id, name, nominal_capacity_liters, material,
                operational_status, version, created_at, updated_at
            ) VALUES (?, ?, 'Tank C-02', 500, 'STAINLESS_STEEL', 'OPERATIONAL', 0,
                      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        vesselId.value(),
        workspaceId.value()
    );
    return vesselId;
  }

  private static void await(@NonNull CountDownLatch latch) {
    try {
      if (!latch.await(5, TimeUnit.SECONDS)) {
        throw new AssertionError("timed out while waiting for concurrent create coordination");
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AssertionError("concurrent create test was interrupted", exception);
    }
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan(basePackages = "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel")
  static class VesselPersistenceConfiguration {
  }
}
