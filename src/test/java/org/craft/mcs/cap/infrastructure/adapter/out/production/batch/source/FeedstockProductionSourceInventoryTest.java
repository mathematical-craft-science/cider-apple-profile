package org.craft.mcs.cap.infrastructure.adapter.out.production.batch.source;

import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchMaterialSnapshotChangedException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.FeedstockMaterialCharacteristicsUnavailableException;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ConsumeFeedstockBatchMaterialCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ConsumeFeedstockBatchMaterialUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ConsumedFeedstockBatchMaterial;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockBatchMaterialEvidenceExpectation;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockBatchMaterialEvidenceReference;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockMaterialCharacteristics;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceNotFoundException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.batch.application.ProductionSourceProfileUnavailableException;
import org.craft.mcs.cap.core.production.batch.application.creation.ProductionSourceEvidenceConflictException;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionSourceEvidenceExpectation;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceCharacteristics;
import org.craft.mcs.cap.core.production.batch.application.port.out.ConsumedProductionSource;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialConsumption;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class FeedstockProductionSourceInventoryTest {

  @Test
  void mapsConsumedFeedstockMaterialToProductionSnapshot() {
    final var useCase = mock(ConsumeFeedstockBatchMaterialUseCase.class);
    final var workspaceId = WorkspaceId.newId();
    final var batchId = FeedstockBatchId.newId();
    final var source = source(batchId, "100");
    final var characteristics = characteristics();
    final var sourceStateId = MaterialStateId.newId();
    final var remainingStateId = MaterialStateId.newId();
    final var expectation = expectation(sourceStateId);
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
        characteristics,
        ConfidenceLevel.LAB_VERIFIED
    ));
    final var inventory = inventory(useCase);

    final var consumed = inventory.consume(workspaceId, source, expectation);

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
        .satisfies(result -> assertThat(result.characteristics())
            .isEqualTo(new ProductionSourceCharacteristics(
                characteristics.specificGravity(),
                characteristics.titratableAcidity(),
                characteristics.tannin(),
                characteristics.yeastAssimilableNitrogen()
            )));
    final var commandCaptor = ArgumentCaptor.forClass(ConsumeFeedstockBatchMaterialCommand.class);
    verify(useCase).consume(commandCaptor.capture());
    assertThat(commandCaptor.getValue().expectation())
        .returns(sourceStateId, FeedstockBatchMaterialEvidenceExpectation::materialStateId)
        .returns(
            new FeedstockBatchMaterialEvidenceReference(expectation.evidenceReference().value()),
            FeedstockBatchMaterialEvidenceExpectation::evidenceReference
        );
  }

  @Test
  void translatesFeedstockNotFoundToProductionSourceNotFound() {
    final var useCase = mock(ConsumeFeedstockBatchMaterialUseCase.class);
    final var batchId = FeedstockBatchId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var source = source(batchId, "100");
    when(useCase.consume(any())).thenThrow(new FeedstockBatchNotFoundException(batchId));
    final var inventory = inventory(useCase);

    assertThatThrownBy(() -> inventory.consume(workspaceId, source, expectation()))
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

    assertThatThrownBy(() -> inventory.consume(expectedWorkspaceId, source, expectation()))
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

    assertThatThrownBy(() -> inventory.consume(workspaceId, source, expectation()))
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

    assertThatThrownBy(() -> inventory.consume(workspaceId, source, expectation()))
        .isInstanceOf(ProductionSourceProfileUnavailableException.class);
  }

  @Test
  void translatesMissingRequiredCharacteristicsToProductionSourceProfileUnavailable() {
    final var useCase = mock(ConsumeFeedstockBatchMaterialUseCase.class);
    final var batchId = FeedstockBatchId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var source = source(batchId, "100");
    when(useCase.consume(any())).thenThrow(new FeedstockMaterialCharacteristicsUnavailableException(batchId));
    final var inventory = inventory(useCase);

    assertThatThrownBy(() -> inventory.consume(workspaceId, source, expectation()))
        .isInstanceOf(ProductionSourceProfileUnavailableException.class);
  }

  @Test
  void translatesChangedFeedstockSnapshotToProductionEvidenceConflict() {
    final var useCase = mock(ConsumeFeedstockBatchMaterialUseCase.class);
    final var batchId = FeedstockBatchId.newId();
    final var source = source(batchId, "100");
    when(useCase.consume(any())).thenThrow(new FeedstockBatchMaterialSnapshotChangedException(batchId));
    final var inventory = inventory(useCase);

    assertThatThrownBy(() -> inventory.consume(WorkspaceId.newId(), source, expectation()))
        .isInstanceOf(ProductionSourceEvidenceConflictException.class)
        .satisfies(exception -> assertThat((ProductionSourceEvidenceConflictException) exception)
            .returns(source.reference(), ProductionSourceEvidenceConflictException::sourceReference));
  }

  private static @NonNull FeedstockProductionSourceInventory inventory(
      @NonNull ConsumeFeedstockBatchMaterialUseCase useCase
  ) {
    return new FeedstockProductionSourceInventory(useCase);
  }

  private static @NonNull FeedstockMaterialCharacteristics characteristics() {
    return new FeedstockMaterialCharacteristics(
        PointCharacteristicValue.of(SpecificGravityValue.of("1.052")),
        PointCharacteristicValue.of(TitratableAcidityValue.of("5.8")),
        null,
        null
    );
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

  private static @NonNull ProductionSourceEvidenceExpectation expectation() {
    return expectation(MaterialStateId.newId());
  }

  private static @NonNull ProductionSourceEvidenceExpectation expectation(
      @NonNull MaterialStateId materialStateId
  ) {
    return new ProductionSourceEvidenceExpectation(
        ProductionSourceEvidenceReference.of("feedstock-profile-v1:measurement-evidence"),
        materialStateId
    );
  }
}
