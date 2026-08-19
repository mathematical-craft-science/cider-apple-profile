package org.craft.mcs.cap.core.production.planning.application.confirmation.port.in;

import org.craft.mcs.cap.core.production.planning.application.confirmation.model.ProductionBatchConfirmationResult;
import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Result containing the materialized batch acknowledgement and now-confirmed candidate view. */
public record ProductionBatchCreated(
    @NonNull ProductionBatchConfirmationResult productionBatch,
    @NonNull ProductionBatchCandidateView candidate
) {

  public ProductionBatchCreated {
    requireNonNull(productionBatch, "production batch cannot be null");
    requireNonNull(candidate, "production batch candidate cannot be null");
  }
}
