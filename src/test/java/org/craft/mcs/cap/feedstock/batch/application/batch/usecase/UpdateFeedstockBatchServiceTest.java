package org.craft.mcs.cap.feedstock.batch.application.batch.usecase;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchVesselChangeNotAllowedException;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.UpdateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchOperationHistory;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.usecase.UpdateFeedstockBatchService;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.usecase.FeedstockBatchViewEnricher;
import org.craft.mcs.cap.core.feedstock.batch.application.vessel.port.out.FeedstockVesselReferenceQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.vessel.port.out.FeedstockVesselAssignmentValidator;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.support.VersionedAggregateFixtures.versioned;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class UpdateFeedstockBatchServiceTest {

  @Test
  void updatesMetadataWithoutChangingMaterialStateProperties() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var enricher = enricher();
    final var service = new UpdateFeedstockBatchService(
        repository,
        mock(FeedstockAccessControl.class),
        transactionHelper(),
        enricher,
        mock(FeedstockVesselAssignmentValidator.class),
        mock(FeedstockBatchOperationHistory.class)
    );
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId, VolumeValue.ofLiters("250.000"));
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(repository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    final var updated = service.update(new UpdateFeedstockBatchCommand(
        batch.batchId(),
        workspaceId,
        null,
        BatchName.of("Renamed batch"),
        FeedstockType.BLEND,
        FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-11-30")),
        null,
        false,
        "metadata update",
        false
    ));

    assertThat(updated)
        .extracting(FeedstockBatchView::batch)
        .returns(BatchName.of("Renamed batch"), FeedstockBatchSummary::name)
        .returns(FeedstockType.BLEND, FeedstockBatchSummary::type)
        .returns(VesselTestFixtures.FEEDSTOCK_VESSEL_ID, FeedstockBatchSummary::vesselId)
        .returns(VolumeValue.ofLiters("250.000"), FeedstockBatchSummary::availableVolume);
    verify(repository).update(any());
  }

  @Test
  void allowsMetadataUpdateAfterBatchWasFullyConsumed() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var enricher = enricher();
    final var service = new UpdateFeedstockBatchService(
        repository,
        mock(FeedstockAccessControl.class),
        transactionHelper(),
        enricher,
        mock(FeedstockVesselAssignmentValidator.class),
        mock(FeedstockBatchOperationHistory.class)
    );
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId, VolumeValue.ofLiters("0"));
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(repository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    final var updated = service.update(new UpdateFeedstockBatchCommand(
        batch.batchId(),
        workspaceId,
        null,
        BatchName.of("Renamed batch"),
        null,
        null,
        null,
        false,
        null,
        false
    ));

    assertThat(updated)
        .extracting(FeedstockBatchView::batch)
        .returns(BatchName.of("Renamed batch"), FeedstockBatchSummary::name)
        .returns(VolumeValue.ofLiters("0"), FeedstockBatchSummary::availableVolume);
    verify(repository).update(any());
  }

  @Test
  void correctsInitialVesselBeforeFirstOperation() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var validator = mock(FeedstockVesselAssignmentValidator.class);
    final var operationHistory = mock(FeedstockBatchOperationHistory.class);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId, VolumeValue.ofLiters("250.000"));
    final var originalMaterialStateId = batch.currentMaterialStateId();
    final var targetVesselId = VesselTestFixtures.vesselId("Tank A-02");
    final var service = new UpdateFeedstockBatchService(
        repository,
        mock(FeedstockAccessControl.class),
        transactionHelper(),
        enricher(targetVesselId, "Tank A-02"),
        validator,
        operationHistory
    );
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(repository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(validator.requireAssignable(workspaceId, targetVesselId, batch.availableVolume()))
        .thenReturn(VesselTestFixtures.reference(targetVesselId, "Tank A-02"));

    final var updated = service.update(new UpdateFeedstockBatchCommand(
        batch.batchId(),
        workspaceId,
        targetVesselId,
        null,
        null,
        null,
        null,
        false,
        null,
        false
    ));

    assertThat(updated.batch())
        .returns(targetVesselId, FeedstockBatchSummary::vesselId)
        .returns(originalMaterialStateId, FeedstockBatchSummary::currentMaterialStateId);
    verify(operationHistory).hasOperations(batch.batchId());
    verify(validator).requireAssignable(workspaceId, targetVesselId, batch.availableVolume());
    verify(repository).update(any());
  }

  @Test
  void rejectsVesselCorrectionAfterFirstOperation() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var validator = mock(FeedstockVesselAssignmentValidator.class);
    final var operationHistory = mock(FeedstockBatchOperationHistory.class);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId, VolumeValue.ofLiters("250.000"));
    final var targetVesselId = VesselTestFixtures.vesselId("Tank A-02");
    final var service = new UpdateFeedstockBatchService(
        repository,
        mock(FeedstockAccessControl.class),
        transactionHelper(),
        enricher(),
        validator,
        operationHistory
    );
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(operationHistory.hasOperations(batch.batchId())).thenReturn(true);
    final var command = new UpdateFeedstockBatchCommand(
        batch.batchId(),
        workspaceId,
        targetVesselId,
        null,
        null,
        null,
        null,
        false,
        null,
        false
    );

    assertThatThrownBy(() -> service.update(command))
        .isInstanceOf(FeedstockBatchVesselChangeNotAllowedException.class)
        .hasMessageContaining("after the first operation");
    verifyNoInteractions(validator);
    verify(repository, never()).update(any());
  }

  @Test
  void leavesInitialVesselUntouchedWithoutConsultingOperationHistory() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var validator = mock(FeedstockVesselAssignmentValidator.class);
    final var operationHistory = mock(FeedstockBatchOperationHistory.class);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId, VolumeValue.ofLiters("250.000"));
    final var service = new UpdateFeedstockBatchService(
        repository,
        mock(FeedstockAccessControl.class),
        transactionHelper(),
        enricher(),
        validator,
        operationHistory
    );
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(repository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.update(new UpdateFeedstockBatchCommand(
        batch.batchId(),
        workspaceId,
        batch.vesselId(),
        BatchName.of("Renamed only"),
        null,
        null,
        null,
        false,
        null,
        false
    ));

    verifyNoInteractions(validator, operationHistory);
    verify(repository).update(any());
  }

  private static @NonNull FeedstockBatch batch(
      @NonNull WorkspaceId workspaceId,
      @NonNull VolumeValue availableVolume
  ) {
    return FeedstockBatch.restore(
        FeedstockBatchId.newId(),
        workspaceId,
        BatchName.of("Late season fruit"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        null,
        availableVolume.isZero()
            ? BatchLifecycle.DEPLETED
            : BatchLifecycle.ACTIVE,
        availableVolume,
        MaterialStateId.newId(),
        null,
        null
    );
  }

  private static @NonNull TransactionHelper transactionHelper() {
    return new TransactionHelper() {
      @NonNull
      @Override
      public <T> T callInTransactionNonNull(@NonNull Supplier<T> action) {
        return action.get();
      }

      @Override
      public void executeInTransaction(@NonNull Runnable action) {
        action.run();
      }
    };
  }

  private static FeedstockBatchViewEnricher enricher() {
    return enricher(VesselTestFixtures.FEEDSTOCK_VESSEL_ID, "Tank A-01");
  }

  private static FeedstockBatchViewEnricher enricher(
      @NonNull VesselId vesselId,
      @NonNull String vesselName
  ) {
    final var referenceQuery = mock(FeedstockVesselReferenceQuery.class);
    when(referenceQuery.resolveReferences(any(), anySet())).thenReturn(java.util.Map.of(
        vesselId,
        VesselTestFixtures.reference(vesselId, vesselName)
    ));
    return new FeedstockBatchViewEnricher(referenceQuery);
  }
}
