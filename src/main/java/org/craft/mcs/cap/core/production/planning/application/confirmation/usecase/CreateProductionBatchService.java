package org.craft.mcs.cap.core.production.planning.application.confirmation.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.planning.application.ProductionBatchCandidateNotFoundException;
import org.craft.mcs.cap.core.production.planning.application.ProductionBatchCandidateOwnershipMismatchException;
import org.craft.mcs.cap.core.production.planning.application.authorization.port.out.ProductionPlanningAccessControl;
import org.craft.mcs.cap.core.production.planning.application.confirmation.model.ProductionBatchConfirmationRequest;
import org.craft.mcs.cap.core.production.planning.application.confirmation.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.planning.application.confirmation.port.in.CreateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.planning.application.confirmation.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.planning.application.confirmation.port.out.ProductionBatchConfirmationGateway;
import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.planning.application.usecase.ProductionBatchCandidateViewMapper;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Confirms an accepted planning candidate after the batch context materializes it atomically.
 *
 * <p>The service owns candidate lifecycle only. Source consumption, vessel validation, batch
 * persistence and trace recording remain behind the batch confirmation gateway.</p>
 */
@Component
@RequiredArgsConstructor
public final class CreateProductionBatchService implements CreateProductionBatchUseCase {

  private final @NonNull ProductionBatchCandidateStore candidateStore;
  private final @NonNull ProductionBatchConfirmationGateway confirmationGateway;
  private final @NonNull ProductionPlanningAccessControl accessControl;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull ProductionBatchCreated create(@NonNull CreateProductionBatchCommand command) {
    requireNonNull(command, "create production batch command cannot be null");
    accessControl.requireWriteAccess(command.workspaceId());

    return transactionHelper.callInTransactionNonNull(() -> confirmAtomically(command));
  }

  private @NonNull ProductionBatchCreated confirmAtomically(
      @NonNull CreateProductionBatchCommand command
  ) {
    final var snapshot = requireCandidate(command);
    final var candidate = snapshot.candidate();
    candidate.requireAvailable();
    final var confirmation = confirmationGateway.confirm(new ProductionBatchConfirmationRequest(
        command.workspaceId(),
        command.name(),
        command.vesselId(),
        candidate.sourceComposition(),
        candidate.sourceStateEvidence(),
        candidate.profile(),
        command.note()
    ));
    candidate.confirm(confirmation.batchId());
    final var confirmedCandidate = candidateStore.update(snapshot.withCandidate(candidate));

    return new ProductionBatchCreated(
        confirmation,
        ProductionBatchCandidateViewMapper.toView(confirmedCandidate)
    );
  }

  private @NonNull ProductionBatchCandidateSnapshot requireCandidate(
      @NonNull CreateProductionBatchCommand command
  ) {
    final var snapshot = candidateStore.findById(command.candidateId())
        .orElseThrow(() -> new ProductionBatchCandidateNotFoundException(command.candidateId()));
    if (!snapshot.candidate().workspaceId().equals(command.workspaceId())) {
      throw new ProductionBatchCandidateOwnershipMismatchException(
          command.workspaceId(),
          snapshot.candidate().workspaceId()
      );
    }
    return snapshot;
  }
}
