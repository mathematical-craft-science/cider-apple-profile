package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.craft.mcs.cap.core.production.application.port.in.ProductionMeasurementChangePolicyUseCase;
import org.craft.mcs.cap.core.production.application.usecase.ProductionMeasurementChangePolicyService;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcProductionBatchRepository.class,
    ProductionMeasurementChangePolicyService.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProductionMeasurementLifecycleLockDataJdbcTest {

  @Autowired
  private JdbcProductionBatchRepository repository;

  @Autowired
  private JdbcTemplate jdbc;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private ProductionMeasurementChangePolicyUseCase measurementChangePolicy;

  @Test
  void measurementLifecyclePolicyRequiresAnOwningTransaction() {
    final var batch = persistBatch(persistWorkspace(), "Unprotected cider", "Tank P-00");

    assertThatThrownBy(() -> measurementChangePolicy.requireChangesAllowed(batch.batchId()))
        .isInstanceOf(IllegalTransactionStateException.class);
  }

  @Test
  void measurementLockSerializesMaterialMutationOfOnlyTheSameBatch() throws Exception {
    final var workspaceId = persistWorkspace();
    final var lockedBatch = persistBatch(workspaceId, "Locked cider", "Tank P-01");
    final var independentBatch = persistBatch(workspaceId, "Independent cider", "Tank P-02");
    final var lockAcquired = new CountDownLatch(1);
    final var releaseLock = new CountDownLatch(1);
    final var sameBatchUpdateStarted = new CountDownLatch(1);
    final var transactions = new TransactionTemplate(transactionManager);

    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var lock = executor.submit(() -> transactions.executeWithoutResult(_ -> {
        measurementChangePolicy.requireChangesAllowed(lockedBatch.batchId());
        lockAcquired.countDown();
        await(releaseLock);
      }));
      assertThat(lockAcquired.await(5, TimeUnit.SECONDS)).isTrue();

      final var independentUpdate = executor.submit(() -> transactions.executeWithoutResult(
          _ -> deplete(independentBatch.batchId())
      ));
      independentUpdate.get(5, TimeUnit.SECONDS);

      final var sameBatchUpdate = executor.submit(() -> transactions.executeWithoutResult(_ -> {
        sameBatchUpdateStarted.countDown();
        deplete(lockedBatch.batchId());
      }));
      assertThat(sameBatchUpdateStarted.await(5, TimeUnit.SECONDS)).isTrue();
      assertThatThrownBy(() -> sameBatchUpdate.get(200, TimeUnit.MILLISECONDS))
          .isInstanceOf(TimeoutException.class);

      releaseLock.countDown();
      lock.get(5, TimeUnit.SECONDS);
      sameBatchUpdate.get(5, TimeUnit.SECONDS);
    } finally {
      releaseLock.countDown();
    }

    assertThat(repository.findById(lockedBatch.batchId()))
        .get()
        .returns(BatchLifecycle.DEPLETED, ProductionBatch::lifecycle);
    assertThat(repository.findById(independentBatch.batchId()))
        .get()
        .returns(BatchLifecycle.DEPLETED, ProductionBatch::lifecycle);
  }

  private void deplete(@NonNull ProductionBatchId batchId) {
    final var state = repository.findVersionedById(batchId).orElseThrow();
    state.aggregate().deplete();
    repository.update(state);
  }

  private @NonNull ProductionBatch persistBatch(
      @NonNull WorkspaceId workspaceId,
      @NonNull String name,
      @NonNull String vesselName
  ) {
    final var vesselId = VesselId.newId();
    VesselTestFixtures.persist(jdbc, workspaceId, vesselId, vesselName);
    return repository.create(ProductionBatch.create(
        ProductionBatchId.newId(),
        workspaceId,
        BatchName.of(name),
        vesselId,
        VolumeValue.ofLiters("100"),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.050"),
            TitratableAcidityValue.of("6.0"),
            null,
            null
        ),
        null
    ));
  }

  private @NonNull WorkspaceId persistWorkspace() {
    final var workspaceId = WorkspaceId.newId();
    jdbc.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, 'Production measurement lifecycle lock', 'CIDERY', 'ACTIVE', 0,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value()
    );
    return workspaceId;
  }

  private static void await(@NonNull CountDownLatch latch) {
    try {
      if (!latch.await(5, TimeUnit.SECONDS)) {
        throw new AssertionError("timed out while holding production measurement lifecycle lock");
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AssertionError("production measurement lifecycle lock test was interrupted", exception);
    }
  }
}
