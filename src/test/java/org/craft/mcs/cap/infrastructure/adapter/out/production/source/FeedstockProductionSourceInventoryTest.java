package org.craft.mcs.cap.infrastructure.adapter.out.production.source;

import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.ConsumeFeedstockBatchMaterialUseCase;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.ConsumedFeedstockBatchMaterial;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockBatchMaterialName;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockBatchMaterialEvidenceReference;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceNotFoundException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.application.ProductionSourceProfileUnavailableException;
import org.craft.mcs.cap.core.production.application.port.out.ConsumedProductionSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
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
    when(useCase.consume(any())).thenReturn(new ConsumedFeedstockBatchMaterial(
        batchId,
        FeedstockBatchMaterialName.of("Late juice"),
        source.volume(),
        new FeedstockBatchMaterialEvidenceReference(
            UUID.fromString("019f0f03-4715-7cbd-a634-a178df719e28"),
            3
        ),
        profile,
        ConfidenceLevel.LAB_VERIFIED
    ));
    final var inventory = inventory(useCase);

    final var consumed = inventory.consume(workspaceId, source);

    assertThat(consumed)
        .returns(source.reference(), ConsumedProductionSource::reference)
        .returns(ProductionSourceDisplayName.of("Late juice"), ConsumedProductionSource::displayName)
        .returns(VolumeValue.ofLiters("100"), ConsumedProductionSource::volume)
        .returns(ConfidenceLevel.LAB_VERIFIED, ConsumedProductionSource::confidence)
        .returns(
            ProductionSourceEvidenceReference.of("019f0f03-4715-7cbd-a634-a178df719e28:v3"),
            ConsumedProductionSource::evidenceReference
        )
        .satisfies(result -> assertThat(result.profile())
            .isNotNull()
            .satisfies(axes -> assertThat(axes.valueOf(ChemicalAxis.SUGAR)).isPresent()));
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
