package org.craft.mcs.cap.production.application.usecase;

import java.util.Optional;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.application.usecase.ProductionMeasurementChangePolicyService;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ProductionMeasurementChangePolicyServiceTest {

  @Test
  void allowsMeasurementChangesForActiveBatch() {
    final var batch = activeBatch();
    final var repository = mock(ProductionBatchRepository.class);
    when(repository.lockForMeasurementChange(batch.batchId())).thenReturn(Optional.of(batch));
    final var service = new ProductionMeasurementChangePolicyService(repository);

    assertThatCode(() -> service.requireChangesAllowed(batch.batchId())).doesNotThrowAnyException();

    verify(repository).lockForMeasurementChange(batch.batchId());
  }

  @ParameterizedTest
  @EnumSource(value = BatchLifecycle.class, names = {"DEPLETED", "DISCARDED"})
  void rejectsMeasurementChangesForInactiveBatch(BatchLifecycle lifecycle) {
    final var batch = inactiveBatch(lifecycle);
    final var repository = mock(ProductionBatchRepository.class);
    when(repository.lockForMeasurementChange(batch.batchId())).thenReturn(Optional.of(batch));
    final var service = new ProductionMeasurementChangePolicyService(repository);

    assertThatThrownBy(() -> service.requireChangesAllowed(batch.batchId()))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("production batch measurements can be changed only when lifecycle is ACTIVE");
  }

  private static ProductionBatch activeBatch() {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Fresh cider"),
        VesselTestFixtures.PRODUCTION_VESSEL_ID,
        VolumeValue.ofLiters("100"),
        profile(),
        null
    );
  }

  private static ProductionBatch inactiveBatch(BatchLifecycle lifecycle) {
    return ProductionBatch.restore(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Finished cider"),
        VesselTestFixtures.PRODUCTION_VESSEL_ID,
        lifecycle,
        AlcoholicFermentationState.COMPLETED,
        VolumeValue.ofLiters("0"),
        MaterialStateId.newId(),
        profile(),
        null
    );
  }

  private static ProductionProfileSnapshot profile() {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0"),
        null,
        null
    );
  }
}
