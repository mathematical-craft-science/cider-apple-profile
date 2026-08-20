package org.craft.mcs.cap.infrastructure.adapter.out.production.planning.source;

import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.FeedstockMaterialCharacteristicsUnavailableException;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockBatchMaterialEvidenceReference;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockBatchMaterialSnapshot;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockMaterialCharacteristics;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.InspectFeedstockBatchMaterialUseCase;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceCharacteristics;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceInsufficientVolumeException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceNotFoundException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.planning.application.ProductionPlanningSourceProfileUnavailableException;
import org.craft.mcs.cap.core.production.planning.application.contract.AvailableProductionSource;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialAvailability;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeedstockProductionSourceInspectorTest {

  @Test
  void mapsFeedstockMaterialToPlanningOwnedSourceSnapshot() {
    final var useCase = mock(InspectFeedstockBatchMaterialUseCase.class);
    final var batchId = FeedstockBatchId.newId();
    final var stateId = MaterialStateId.newId();
    final var source = source(batchId);
    final var characteristics = characteristics();
    when(useCase.inspect(any())).thenReturn(new FeedstockBatchMaterialSnapshot(
        batchId,
        BatchName.of("Late juice"),
        AggregateRevision.of(3),
        new MaterialAvailability(source.volume(), VolumeValue.ofLiters("250"), stateId),
        new FeedstockBatchMaterialEvidenceReference("feedstock-profile-v1:measurement-evidence"),
        characteristics,
        ConfidenceLevel.LAB_VERIFIED
    ));

    final var inspected = new FeedstockProductionSourceInspector(useCase)
        .inspect(WorkspaceId.newId(), source);

    assertThat(inspected)
        .returns(source.reference(), AvailableProductionSource::reference)
        .returns(ProductionSourceDisplayName.of("Late juice"), AvailableProductionSource::displayName)
        .returns(AggregateRevision.of(3), AvailableProductionSource::aggregateRevision)
        .returns(VolumeValue.ofLiters("100"), AvailableProductionSource::selectedVolume)
        .returns(VolumeValue.ofLiters("250"), AvailableProductionSource::availableVolume)
        .returns(stateId, AvailableProductionSource::materialStateId)
        .returns(ConfidenceLevel.LAB_VERIFIED, AvailableProductionSource::confidence)
        .returns(
            ProductionSourceEvidenceReference.of("feedstock-profile-v1:measurement-evidence"),
            AvailableProductionSource::evidenceReference
        )
        .satisfies(result -> assertThat(result.characteristics())
            .isEqualTo(new ProductionSourceCharacteristics(
                characteristics.specificGravity(),
                characteristics.titratableAcidity(),
                characteristics.tannin(),
                characteristics.yeastAssimilableNitrogen()
            )));
  }

  @Test
  void translatesNotFoundToPlanningLanguage() {
    final var batchId = FeedstockBatchId.newId();
    final var useCase = failingUseCase(new FeedstockBatchNotFoundException(batchId));

    assertThatThrownBy(() -> inspect(useCase, batchId))
        .isInstanceOf(ProductionPlanningSourceNotFoundException.class);
  }

  @Test
  void translatesOwnershipMismatchToPlanningLanguageWithoutLosingWorkspaceFacts() {
    final var batchId = FeedstockBatchId.newId();
    final var expectedWorkspaceId = WorkspaceId.newId();
    final var actualWorkspaceId = WorkspaceId.newId();
    final var useCase = failingUseCase(new FeedstockBatchOwnershipMismatchException(
        batchId,
        expectedWorkspaceId,
        actualWorkspaceId
    ));

    assertThatThrownBy(() -> inspect(useCase, batchId))
        .isInstanceOf(ProductionPlanningSourceOwnershipMismatchException.class)
        .satisfies(exception -> assertThat((ProductionPlanningSourceOwnershipMismatchException) exception)
            .returns(
                expectedWorkspaceId,
                ProductionPlanningSourceOwnershipMismatchException::expectedWorkspaceId
            )
            .returns(
                actualWorkspaceId,
                ProductionPlanningSourceOwnershipMismatchException::actualWorkspaceId
            ));
  }

  @Test
  void translatesInsufficientVolumeToPlanningLanguageWithoutLosingVolumeFacts() {
    final var batchId = FeedstockBatchId.newId();
    final var requestedVolume = VolumeValue.ofLiters("100");
    final var availableVolume = VolumeValue.ofLiters("80");
    final var useCase = failingUseCase(new FeedstockBatchInsufficientAvailableVolumeException(
        batchId,
        requestedVolume,
        availableVolume
    ));

    assertThatThrownBy(() -> inspect(useCase, batchId))
        .isInstanceOf(ProductionPlanningSourceInsufficientVolumeException.class)
        .satisfies(exception -> assertThat((ProductionPlanningSourceInsufficientVolumeException) exception)
            .returns(requestedVolume, ProductionPlanningSourceInsufficientVolumeException::requestedVolume)
            .returns(availableVolume, ProductionPlanningSourceInsufficientVolumeException::availableVolume));
  }

  @Test
  void translatesMissingMeasurementsToPlanningLanguage() {
    final var batchId = FeedstockBatchId.newId();
    final var useCase = failingUseCase(new FeedstockBatchHasNoMeasurementsException(batchId));

    assertThatThrownBy(() -> inspect(useCase, batchId))
        .isInstanceOf(ProductionPlanningSourceProfileUnavailableException.class);
  }

  @Test
  void translatesMissingCharacteristicsToPlanningLanguage() {
    final var batchId = FeedstockBatchId.newId();
    final var useCase = failingUseCase(new FeedstockMaterialCharacteristicsUnavailableException(batchId));

    assertThatThrownBy(() -> inspect(useCase, batchId))
        .isInstanceOf(ProductionPlanningSourceProfileUnavailableException.class);
  }

  private static @NonNull InspectFeedstockBatchMaterialUseCase failingUseCase(
      @NonNull RuntimeException exception
  ) {
    final var useCase = mock(InspectFeedstockBatchMaterialUseCase.class);
    when(useCase.inspect(any())).thenThrow(exception);
    return useCase;
  }

  private static void inspect(
      @NonNull InspectFeedstockBatchMaterialUseCase useCase,
      @NonNull FeedstockBatchId batchId
  ) {
    new FeedstockProductionSourceInspector(useCase).inspect(WorkspaceId.newId(), source(batchId));
  }

  private static @NonNull FeedstockMaterialCharacteristics characteristics() {
    return new FeedstockMaterialCharacteristics(
        PointCharacteristicValue.of(SpecificGravityValue.of("1.052")),
        PointCharacteristicValue.of(TitratableAcidityValue.of("5.8")),
        null,
        null
    );
  }

  private static @NonNull ProductionSourceComponent source(@NonNull FeedstockBatchId batchId) {
    return new ProductionSourceComponent(
        ProductionSourceReference.feedstockBatch(batchId.value()),
        VolumeValue.ofLiters("100")
    );
  }
}
