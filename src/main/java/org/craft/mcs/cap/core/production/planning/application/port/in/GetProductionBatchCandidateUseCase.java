package org.craft.mcs.cap.core.production.planning.application.port.in;

import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateId;
import org.jspecify.annotations.NonNull;


/** Reads a saved production candidate available to the current workspace actor. */
public interface GetProductionBatchCandidateUseCase {

  @NonNull ProductionBatchCandidateView get(@NonNull ProductionBatchCandidateId candidateId);
}
