package org.craft.mcs.cap.core.production.candidate.application.confirmation.port.in;

import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Result containing the created batch view and the now-confirmed candidate view. */
public record ProductionBatchCreated(
    @NonNull ProductionBatchView productionBatch,
    @NonNull VesselReferenceView vessel,
    @NonNull ProductionBatchCandidateView candidate
) {

  public ProductionBatchCreated {
    requireNonNull(productionBatch, "production batch cannot be null");
    requireNonNull(vessel, "production vessel reference cannot be null");
    if (!productionBatch.vesselId().equals(vessel.vesselId())) {
      throw new ApplicationValidationException(
          "production vessel reference must match batch vessel id"
      );
    }
    requireNonNull(candidate, "production batch candidate cannot be null");
  }
}
