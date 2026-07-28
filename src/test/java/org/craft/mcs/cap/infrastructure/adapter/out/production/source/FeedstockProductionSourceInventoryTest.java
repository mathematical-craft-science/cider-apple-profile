package org.craft.mcs.cap.infrastructure.adapter.out.production.source;

import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.ConsumeFeedstockBatchMaterialUseCase;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.ConsumedFeedstockBatchMaterial;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockBatchMaterialEvidenceReference;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceNotFoundException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.application.ProductionSourceProfileUnavailableException;
import org.craft.mcs.cap.core.production.application.port.out.ConsumedProductionSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialConsumption;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class FeedstockProductionSourceInventoryTest {

  @Test
  void mapsConsumedFeedstockMaterialToProductionSnapshot() {
    final var useCase = mock(ConsumeFeedstockBatchMaterialUseCase.class);
    final var workspaceId = WorkspaceId.newId();
    final var batchId = FeedstockBatchId.newId();
    final var source = source(batchId, "100");
    final var profile = profile();
    final var sourceStateId = MaterialStateId.newId();
    final var remainingStateId = MaterialStateId.newId();
    when(useCase.consume(any())).thenReturn(new ConsumedFeedstockBatchMaterial(
        batchId,
        BatchName.of("Late juice"),
        new MaterialConsumption(
            VolumeValue.ofLiters("250"),
            source.volume(),
            VolumeValue.ofLiters("150"),
            sourceStateId,
            remainingStateId
        ),
        new FeedstockBatchMaterialEvidenceReference("feedstock-profile-v1:measurement-evidence"),
        profile,
        ConfidenceLevel.LAB_VERIFIED
    ));
    final var inventory = inventory(useCase);

    final var consumed = inventory.consume(workspaceId, source);

    assertThat(consumed)
        .returns(source.reference(), ConsumedProductionSource::reference)
        .returns(ProductionSourceDisplayName.of("Late juice"), ConsumedProductionSource::displayName)
        .returns(VolumeValue.ofLiters("250"), ConsumedProductionSource::availableBefore)
        .returns(VolumeValue.ofLiters("100"), ConsumedProductionSource::consumed)
        .returns(VolumeValue.ofLiters("150"), ConsumedProductionSource::remaining)
        .returns(sourceStateId, ConsumedProductionSource::sourceStateId)
        .returns(remainingStateId, ConsumedProductionSource::remainingSourceStateId)
        .returns(ConfidenceLevel.LAB_VERIFIED, ConsumedProductionSource::confidence)
        .returns(
            ProductionSourceEvidenceReference.of("feedstock-profile-v1:measurement-evidence"),
            ConsumedProductionSource::evidenceReference
        )
        .satisfies(result -> assertThat(result.profile())
            .isNotNull()
            .satisfies(axes -> assertThat(axes.valueOf(ChemicalAxis.SPECIFIC_GRAVITY)).isPresent()));
  }

  @Test
  void translatesFeedstockNotFoundToProductionSourceNotFound() {
    final var useCase = mock(ConsumeFeedstockBatchMaterialUseCase.class);
    final var batchId = FeedstockBatchId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var source = source(batchId, "100");
    when(useCase.consume(any())).thenThrow(new FeedstockBatchNotFoundException(batchId));
    final var inventory = inventory(useCase);

    assertThatThrownBy(() -> inventory.consume(workspaceId, source))
        .isInstanceOf(ProductionSourceNotFoundException.class);
  }

  @Test
  void translatesFeedstockOwnershipMismatchWithoutLosingWorkspaceFacts() {
    final var useCase = mock(ConsumeFeedstockBatchMaterialUseCase.class);
    final var batchId = FeedstockBatchId.newId();
    final var expectedWorkspaceId = WorkspaceId.newId();
    final var actualWorkspaceId = WorkspaceId.newId();
    when(useCase.consume(any())).thenThrow(new FeedstockBatchOwnershipMismatchException(
        batchId,
        expectedWorkspaceId,
        actualWorkspaceId
    ));
    final var source = source(batchId, "100");
    final var inventory = inventory(useCase);

    assertThatThrownBy(() -> inventory.consume(expectedWorkspaceId, source))
        .isInstanceOf(ProductionSourceOwnershipMismatchException.class)
        .satisfies(exception -> assertThat((ProductionSourceOwnershipMismatchException) exception)
            .returns(expectedWorkspaceId, ProductionSourceOwnershipMismatchException::expectedWorkspaceId)
            .returns(actualWorkspaceId, ProductionSourceOwnershipMismatchException::actualWorkspaceId));
  }

  @Test
  void translatesInsufficientFeedstockVolumeWithoutLosingVolumeFacts() {
    final var useCase = mock(ConsumeFeedstockBatchMaterialUseCase.class);
    final var batchId = FeedstockBatchId.newId();
    final var requestedVolume = VolumeValue.ofLiters("100");
    final var availableVolume = VolumeValue.ofLiters("80");
    when(useCase.consume(any())).thenThrow(new FeedstockBatchInsufficientAvailableVolumeException(
        batchId,
        requestedVolume,
        availableVolume
    ));
    final var workspaceId = WorkspaceId.newId();
    final var source = source(batchId, "100");
    final var inventory = inventory(useCase);

    assertThatThrownBy(() -> inventory.consume(workspaceId, source))
        .isInstanceOf(ProductionSourceInsufficientAvailableVolumeException.class)
        .satisfies(exception -> assertThat((ProductionSourceInsufficientAvailableVolumeException) exception)
            .returns(requestedVolume, ProductionSourceInsufficientAvailableVolumeException::requestedVolume)
            .returns(availableVolume, ProductionSourceInsufficientAvailableVolumeException::availableVolume));
  }

  @Test
  void translatesMissingFeedstockProfileToProductionSourceProfileUnavailable() {
    final var useCase = mock(ConsumeFeedstockBatchMaterialUseCase.class);
    final var batchId = FeedstockBatchId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var source = source(batchId, "100");
    when(useCase.consume(any())).thenThrow(new FeedstockBatchHasNoMeasurementsException(batchId));
    final var inventory = inventory(useCase);

    assertThatThrownBy(() -> inventory.consume(workspaceId, source))
        .isInstanceOf(ProductionSourceProfileUnavailableException.class);
  }

  private static @NonNull FeedstockProductionSourceInventory inventory(
      @NonNull ConsumeFeedstockBatchMaterialUseCase useCase
  ) {
    return new FeedstockProductionSourceInventory(useCase, new ChemicalProfileAxisExtractor());
  }

  private static @NonNull ChemicalProfile profile() {
    return ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(
            SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))
        ))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.8")))
        .build();
  }

  private static @NonNull ProductionSourceComponent source(
      @NonNull FeedstockBatchId batchId,
      @NonNull String volumeLiters
  ) {
    return new ProductionSourceComponent(
        ProductionSourceReference.feedstockBatch(batchId.value()),
        VolumeValue.ofLiters(volumeLiters)
    );
  }
}
