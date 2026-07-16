package org.craft.mcs.cap.core.production.candidate.application.port.in;

import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateView;
import org.jspecify.annotations.NonNull;


/** Assesses authoritative source state and saves a candidate without consuming source material. */
public interface CreateProductionBatchCandidateUseCase {

  @NonNull ProductionBatchCandidateView create(@NonNull CreateProductionBatchCandidateCommand command);
}
