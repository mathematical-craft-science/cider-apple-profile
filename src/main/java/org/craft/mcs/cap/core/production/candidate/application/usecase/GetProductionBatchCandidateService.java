package org.craft.mcs.cap.core.production.candidate.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.candidate.application.ProductionBatchCandidateNotFoundException;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.candidate.application.port.in.GetProductionBatchCandidateUseCase;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Reads a saved production candidate after checking its workspace access. */
@Component
@RequiredArgsConstructor
public final class GetProductionBatchCandidateService implements GetProductionBatchCandidateUseCase {

  private final @NonNull ProductionBatchCandidateStore candidateStore;
  private final @NonNull ProductionAccessControl productionAccessControl;

  @Override
  public @NonNull ProductionBatchCandidateView get(@NonNull ProductionBatchCandidateId candidateId) {
    requireNonNull(candidateId, "production batch candidate id cannot be null");
    final var snapshot = candidateStore.findById(candidateId)
        .orElseThrow(() -> new ProductionBatchCandidateNotFoundException(candidateId));
    productionAccessControl.requireReadAccess(snapshot.candidate().workspaceId());
    return ProductionBatchCandidateViewMapper.toView(snapshot);
  }
}
