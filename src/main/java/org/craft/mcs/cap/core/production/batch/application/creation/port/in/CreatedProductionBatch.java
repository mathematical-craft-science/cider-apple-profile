package org.craft.mcs.cap.core.production.batch.application.creation.port.in;

import org.craft.mcs.cap.core.production.batch.application.model.ProductionBatchView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Batch-owned result returned after atomically creating material from an accepted plan. */
public record CreatedProductionBatch(
    @NonNull ProductionBatchView batch,
    @NonNull VesselReferenceView vessel
) {

  public CreatedProductionBatch {
    requireNonNull(batch, "created production batch cannot be null");
    requireNonNull(vessel, "production vessel cannot be null");
    if (!batch.vesselId().equals(vessel.vesselId())) {
      throw new ApplicationValidationException(
          "production vessel reference must match created batch vessel id"
      );
    }
  }
}
