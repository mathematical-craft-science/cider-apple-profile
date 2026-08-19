package org.craft.mcs.cap.core.production.batch.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.batch.application.ProductionBatchNotFoundException;
import org.craft.mcs.cap.core.production.batch.application.ProductionBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.production.batch.application.ProductionBatchVesselChangeNotAllowedException;
import org.craft.mcs.cap.core.production.batch.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionBatchDetailsView;
import org.craft.mcs.cap.core.production.batch.application.port.in.UpdateProductionBatchCommand;
import org.craft.mcs.cap.core.production.batch.application.port.in.UpdateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchOperationHistory;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.batch.application.vessel.port.out.ProductionVesselAssignmentValidator;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatch;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Replaces editable production metadata and permits only pre-operation vessel correction. */
@Component
@RequiredArgsConstructor
public final class UpdateProductionBatchService implements UpdateProductionBatchUseCase {

  private final @NonNull ProductionBatchRepository productionBatchRepository;
  private final @NonNull ProductionAccessControl productionAccessControl;
  private final @NonNull ProductionVesselAssignmentValidator vesselAssignmentValidator;
  private final @NonNull ProductionBatchOperationHistory operationHistory;
  private final @NonNull ProductionBatchDetailsAssembler detailsAssembler;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull ProductionBatchDetailsView update(@NonNull UpdateProductionBatchCommand command) {
    requireNonNull(command, "update production batch command cannot be null");
    productionAccessControl.requireWriteAccess(command.workspaceId());
    return transactionHelper.callInTransactionNonNull(() -> updateInTransaction(command));
  }

  private @NonNull ProductionBatchDetailsView updateInTransaction(
      @NonNull UpdateProductionBatchCommand command
  ) {
    final var state = findOwnedBatch(command);
    final var batch = state.aggregate();
    applyVesselChange(batch, command);
    batch.rename(command.name());
    batch.changeNote(command.note());
    final var updated = productionBatchRepository.update(state.withAggregate(batch));
    return detailsAssembler.assemble(ProductionBatchViewMapper.toView(updated.aggregate()));
  }

  private @NonNull VersionedAggregate<ProductionBatch> findOwnedBatch(
      @NonNull UpdateProductionBatchCommand command
  ) {
    final var state = productionBatchRepository.findVersionedById(command.batchId())
        .orElseThrow(() -> new ProductionBatchNotFoundException(command.batchId()));
    final var batch = state.aggregate();
    if (!batch.workspaceId().equals(command.workspaceId())) {
      throw new ProductionBatchOwnershipMismatchException(
          batch.batchId(),
          command.workspaceId(),
          batch.workspaceId()
      );
    }
    return state;
  }

  private void applyVesselChange(
      @NonNull ProductionBatch batch,
      @NonNull UpdateProductionBatchCommand command
  ) {
    if (command.vesselId().equals(batch.vesselId())) {
      return;
    }
    if (operationHistory.hasOperations(batch.batchId())) {
      throw new ProductionBatchVesselChangeNotAllowedException(batch.batchId());
    }
    vesselAssignmentValidator.requireAssignable(
        batch.workspaceId(),
        command.vesselId(),
        batch.currentVolume()
    );
    batch.correctInitialVesselAssignment(command.vesselId());
  }
}
