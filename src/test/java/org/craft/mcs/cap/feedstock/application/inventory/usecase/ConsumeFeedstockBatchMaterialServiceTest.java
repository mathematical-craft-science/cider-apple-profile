package org.craft.mcs.cap.feedstock.application.inventory.usecase;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.ConsumeFeedstockBatchMaterialCommand;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.ConsumedFeedstockBatchMaterial;
import org.craft.mcs.cap.core.feedstock.application.inventory.FeedstockMaterialCharacteristicsUnavailableException;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockMaterialCharacteristics;
import org.craft.mcs.cap.core.feedstock.application.inventory.usecase.ConsumeFeedstockBatchMaterialService;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.craft.mcs.cap.support.VersionedAggregateFixtures.versioned;


class ConsumeFeedstockBatchMaterialServiceTest {

  @Test
  void consumesOwnedMaterialAndReturnsCurrentProfileSnapshot() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var measurementQuery = mock(FeedstockCurrentProfileQueryService.class);
    final var accessControl = mock(FeedstockAccessControl.class);
    final var workspaceId = WorkspaceId.newId();
    final var batch = batch(workspaceId);
    final var measurement = measurement(batch.batchId());
    final var command = command(workspaceId, batch.batchId(), "100");
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(measurementQuery.requireCurrentProfile(batch.batchId())).thenReturn(currentProfile(measurement));
    final var service = service(repository, measurementQuery, accessControl);

    final var consumed = service.consume(command);

    assertThat(consumed)
        .returns(batch.batchId(), ConsumedFeedstockBatchMaterial::batchId)
        .returns(batch.name(), ConsumedFeedstockBatchMaterial::batchName)
        .returns(VolumeValue.ofLiters("250"), ConsumedFeedstockBatchMaterial::availableBefore)
        .returns(command.volume(), ConsumedFeedstockBatchMaterial::consumed)
        .returns(VolumeValue.ofLiters("150"), ConsumedFeedstockBatchMaterial::remaining)
        .satisfies(result -> assertThat(result.characteristics())
            .returns(
                currentProfile(measurement).specificGravity(),
                FeedstockMaterialCharacteristics::specificGravity
            )
            .returns(
                currentProfile(measurement).titratableAcidity(),
                FeedstockMaterialCharacteristics::titratableAcidity
            ))
        .satisfies(result -> assertThat(result.remainingStateId())
            .isNotEqualTo(result.sourceStateId()))
        .satisfies(result -> assertThat(batch.availableVolume()).isEqualTo(VolumeValue.ofLiters("150")));
    verify(accessControl).requireWriteAccess(workspaceId);
    verify(repository).update(any());
  }

  @Test
  void rejectsMaterialOwnedByAnotherWorkspaceBeforeReadingProfile() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var measurementQuery = mock(FeedstockCurrentProfileQueryService.class);
    final var accessControl = mock(FeedstockAccessControl.class);
    final var actualWorkspaceId = WorkspaceId.newId();
    final var requestedWorkspaceId = WorkspaceId.newId();
    final var batch = batch(actualWorkspaceId);
    final var command = command(requestedWorkspaceId, batch.batchId(), "100");
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    final var service = service(repository, measurementQuery, accessControl);

    assertThatThrownBy(() -> service.consume(command))
        .isInstanceOf(FeedstockBatchOwnershipMismatchException.class);
    verify(repository, never()).update(any());
    verifyNoInteractions(measurementQuery);
  }

  @Test
  void reportsAvailableVolumeBeforeMutatingBatch() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var measurementQuery = mock(FeedstockCurrentProfileQueryService.class);
    final var accessControl = mock(FeedstockAccessControl.class);
    final var workspaceId = WorkspaceId.newId();
    final var batch = batch(workspaceId);
    final var command = command(workspaceId, batch.batchId(), "251");
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    final var service = service(repository, measurementQuery, accessControl);

    assertThatThrownBy(() -> service.consume(command))
        .isInstanceOf(FeedstockBatchInsufficientAvailableVolumeException.class)
        .satisfies(exception -> assertThat((FeedstockBatchInsufficientAvailableVolumeException) exception)
            .returns(command.volume(), FeedstockBatchInsufficientAvailableVolumeException::requestedVolume)
            .returns(batch.availableVolume(), FeedstockBatchInsufficientAvailableVolumeException::availableVolume));
    verify(repository, never()).update(any());
    verifyNoInteractions(measurementQuery);
  }

  @Test
  void keepsVolumeUnchangedWhenCurrentProfileIsUnavailable() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var measurementQuery = mock(FeedstockCurrentProfileQueryService.class);
    final var accessControl = mock(FeedstockAccessControl.class);
    final var workspaceId = WorkspaceId.newId();
    final var batch = batch(workspaceId);
    final var command = command(workspaceId, batch.batchId(), "100");
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(measurementQuery.requireCurrentProfile(batch.batchId()))
        .thenThrow(new FeedstockBatchHasNoMeasurementsException(batch.batchId()));
    final var service = service(repository, measurementQuery, accessControl);

    assertThatThrownBy(() -> service.consume(command))
        .isInstanceOf(FeedstockBatchHasNoMeasurementsException.class)
        .satisfies(exception -> assertThat(batch.availableVolume())
            .isEqualTo(VolumeValue.ofLiters("250")));
    verify(repository, never()).update(any());
  }

  @Test
  void keepsVolumeUnchangedWhenCurrentProfileCannotSupportComposition() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var measurementQuery = mock(FeedstockCurrentProfileQueryService.class);
    final var accessControl = mock(FeedstockAccessControl.class);
    final var workspaceId = WorkspaceId.newId();
    final var batch = batch(workspaceId);
    final var command = command(workspaceId, batch.batchId(), "100");
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(measurementQuery.requireCurrentProfile(batch.batchId()))
        .thenReturn(currentProfile(measurementWithoutAcidity(batch.batchId())));
    final var service = service(repository, measurementQuery, accessControl);

    assertThatThrownBy(() -> service.consume(command))
        .isInstanceOf(FeedstockMaterialCharacteristicsUnavailableException.class)
        .satisfies(exception -> assertThat(batch.availableVolume())
            .isEqualTo(VolumeValue.ofLiters("250")));
    verify(repository, never()).update(any());
  }

  private static @NonNull ConsumeFeedstockBatchMaterialService service(
      @NonNull FeedstockBatchRepository repository,
      @NonNull FeedstockCurrentProfileQueryService measurementQuery,
      @NonNull FeedstockAccessControl accessControl
  ) {
    return new ConsumeFeedstockBatchMaterialService(
        repository,
        measurementQuery,
        accessControl,
        transactionHelper()
    );
  }

  private static @NonNull FeedstockBatch batch(@NonNull WorkspaceId workspaceId) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        workspaceId,
        BatchName.of("Late juice"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250")
    );
  }

  private static @NonNull FeedstockMeasurementFixture measurement(@NonNull FeedstockBatchId batchId) {
    return FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        batchId,
        MeasurementSetName.of("Current profile"),
        ProfileCharacteristicsFixture.builder()
            .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of("1.052")))
            .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("6.2")))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
  }

  private static @NonNull FeedstockMeasurementFixture measurementWithoutAcidity(
      @NonNull FeedstockBatchId batchId
  ) {
    return FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        batchId,
        MeasurementSetName.of("Partial profile"),
        ProfileCharacteristicsFixture.builder()
            .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of("1.052")))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
  }

  private static @NonNull ConsumeFeedstockBatchMaterialCommand command(
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockBatchId batchId,
      @NonNull String volumeLiters
  ) {
    return new ConsumeFeedstockBatchMaterialCommand(
        workspaceId,
        batchId,
        VolumeValue.ofLiters(volumeLiters)
    );
  }

  private static @NonNull TransactionHelper transactionHelper() {
    return new TransactionHelper() {
      @Override
      public @NonNull <T> T callInTransactionNonNull(@NonNull Supplier<T> supplier) {
        return supplier.get();
      }

      @Override
      public void executeInTransaction(@NonNull Runnable runnable) {
        runnable.run();
      }
    };
  }
}
