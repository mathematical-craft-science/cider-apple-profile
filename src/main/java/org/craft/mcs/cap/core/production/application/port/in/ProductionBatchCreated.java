package org.craft.mcs.cap.core.production.application.port.in;

import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ProductionBatchCreated(
    @NonNull ProductionBatchView productionBatch,
    @NonNull ProductionBatchCandidateView candidate
) {

  public ProductionBatchCreated {
    requireNonNull(productionBatch, "production batch cannot be null");
    requireNonNull(candidate, "production batch candidate cannot be null");
  }
}
