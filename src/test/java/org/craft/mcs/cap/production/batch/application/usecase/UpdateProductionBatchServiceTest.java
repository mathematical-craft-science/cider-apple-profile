package org.craft.mcs.cap.production.batch.application.usecase;

import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.production.batch.application.ProductionBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.production.batch.application.ProductionBatchVesselChangeNotAllowedException;
import org.craft.mcs.cap.core.production.batch.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionBatchDetailsView;
import org.craft.mcs.cap.core.production.batch.application.port.in.UpdateProductionBatchCommand;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchOperationHistory;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.batch.application.usecase.ProductionBatchDetailsAssembler;
import org.craft.mcs.cap.core.production.batch.application.usecase.UpdateProductionBatchService;
import org.craft.mcs.cap.core.production.batch.application.vessel.port.out.ProductionVesselAssignmentValidator;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.support.VersionedAggregateFixtures.versioned;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class UpdateProductionBatchServiceTest {

  @Test
  void correctsInitialVesselBeforeFirstOperation() {
    final var repository = mock(ProductionBatchRepository.class);
    final var validator = mock(ProductionVesselAssignmentValidator.class);
    final var operationHistory = mock(ProductionBatchOperationHistory.class);
    final var detailsAssembler = mock(ProductionBatchDetailsAssembler.class);
    final var details = mock(ProductionBatchDetailsView.class);
    final var batch = batch();
    final var originalMaterialStateId = batch.currentMaterialStateId();
    final var targetVesselId = VesselTestFixtures.vesselId("Tank F-04");
    final var service = service(repository, validator, operationHistory, detailsAssembler);
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(repository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(validator.requireAssignable(batch.workspaceId(), targetVesselId, batch.currentVolume()))
        .thenReturn(VesselTestFixtures.reference(targetVesselId, "Tank F-04"));
    when(detailsAssembler.assemble(any())).thenReturn(details);

    final var result = service.update(new UpdateProductionBatchCommand(
        batch.batchId(),
        batch.workspaceId(),
        BatchName.of("Corrected cider"),
        targetVesselId,
        "Corrected initial placement"
    ));

    assertThat(result).isSameAs(details);
    assertThat(batch)
        .returns(BatchName.of("Corrected cider"), ProductionBatch::name)
        .returns(targetVesselId, ProductionBatch::vesselId)
        .returns("Corrected initial placement", ProductionBatch::note)
        .returns(originalMaterialStateId, ProductionBatch::currentMaterialStateId);
    verify(operationHistory).hasOperations(batch.batchId());
    verify(validator).requireAssignable(batch.workspaceId(), targetVesselId, batch.currentVolume());
    verify(repository).update(any());
  }

  @Test
  void rejectsVesselCorrectionAfterFirstOperation() {
    final var repository = mock(ProductionBatchRepository.class);
    final var validator = mock(ProductionVesselAssignmentValidator.class);
    final var operationHistory = mock(ProductionBatchOperationHistory.class);
    final var detailsAssembler = mock(ProductionBatchDetailsAssembler.class);
    final var batch = batch();
    final var targetVesselId = VesselTestFixtures.vesselId("Tank F-04");
    final var service = service(repository, validator, operationHistory, detailsAssembler);
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(operationHistory.hasOperations(batch.batchId())).thenReturn(true);
    final var command = new UpdateProductionBatchCommand(
        batch.batchId(),
        batch.workspaceId(),
        batch.name(),
        targetVesselId,
        batch.note()
    );

    assertThatThrownBy(() -> service.update(command))
        .isInstanceOf(ProductionBatchVesselChangeNotAllowedException.class)
        .hasMessageContaining("after the first operation");
    verifyNoInteractions(validator, detailsAssembler);
    verify(repository, never()).update(any());
  }

  @Test
  void leavesInitialVesselUntouchedWithoutConsultingOperationHistory() {
    final var repository = mock(ProductionBatchRepository.class);
    final var validator = mock(ProductionVesselAssignmentValidator.class);
    final var operationHistory = mock(ProductionBatchOperationHistory.class);
    final var detailsAssembler = mock(ProductionBatchDetailsAssembler.class);
    final var details = mock(ProductionBatchDetailsView.class);
    final var batch = batch();
    final var service = service(repository, validator, operationHistory, detailsAssembler);
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    when(repository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(detailsAssembler.assemble(any())).thenReturn(details);

    service.update(new UpdateProductionBatchCommand(
        batch.batchId(),
        batch.workspaceId(),
        BatchName.of("Renamed only"),
        batch.vesselId(),
        null
    ));

    verifyNoInteractions(validator, operationHistory);
    verify(repository).update(any());
  }

  @Test
  void rejectsUpdateThroughAnotherWorkspaceBeforeInspectingOperationHistory() {
    final var repository = mock(ProductionBatchRepository.class);
    final var validator = mock(ProductionVesselAssignmentValidator.class);
    final var operationHistory = mock(ProductionBatchOperationHistory.class);
    final var detailsAssembler = mock(ProductionBatchDetailsAssembler.class);
    final var batch = batch();
    final var requestedWorkspaceId = WorkspaceId.newId();
    final var service = service(repository, validator, operationHistory, detailsAssembler);
    when(repository.findVersionedById(batch.batchId())).thenReturn(Optional.of(versioned(batch)));
    final var command = new UpdateProductionBatchCommand(
        batch.batchId(),
        requestedWorkspaceId,
        batch.name(),
        VesselTestFixtures.vesselId("Tank F-04"),
        batch.note()
    );

    assertThatThrownBy(() -> service.update(command))
        .isInstanceOf(ProductionBatchOwnershipMismatchException.class)
        .satisfies(exception -> assertThat((ProductionBatchOwnershipMismatchException) exception)
            .returns(batch.batchId(), ProductionBatchOwnershipMismatchException::batchId)
            .returns(requestedWorkspaceId, ProductionBatchOwnershipMismatchException::expectedWorkspaceId)
            .returns(batch.workspaceId(), ProductionBatchOwnershipMismatchException::actualWorkspaceId));
    verifyNoInteractions(validator, operationHistory, detailsAssembler);
    verify(repository, never()).update(any());
  }

  private static @NonNull UpdateProductionBatchService service(
      @NonNull ProductionBatchRepository repository,
      @NonNull ProductionVesselAssignmentValidator validator,
      @NonNull ProductionBatchOperationHistory operationHistory,
      @NonNull ProductionBatchDetailsAssembler detailsAssembler
  ) {
    return new UpdateProductionBatchService(
        repository,
        mock(ProductionAccessControl.class),
        validator,
        operationHistory,
        detailsAssembler,
        transactionHelper()
    );
  }

  private static @NonNull ProductionBatch batch() {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("October cider"),
        VesselTestFixtures.vesselId("Tank F-03"),
        VolumeValue.ofLiters("120"),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.052"),
            TitratableAcidityValue.of("6.2"),
            null,
            null
        ),
        null
    );
  }

  private static @NonNull TransactionHelper transactionHelper() {
    return new TransactionHelper() {
      @Override
      public <T> @NonNull T callInTransactionNonNull(@NonNull Supplier<T> action) {
        return action.get();
      }

      @Override
      public void executeInTransaction(@NonNull Runnable action) {
        action.run();
      }
    };
  }
}
