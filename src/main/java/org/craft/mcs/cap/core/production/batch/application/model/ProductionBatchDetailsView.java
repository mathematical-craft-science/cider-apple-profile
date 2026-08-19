package org.craft.mcs.cap.core.production.batch.application.model;

import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Production batch read model enriched with derived analysis required by read scenarios. */
public record ProductionBatchDetailsView(
    @NonNull ProductionBatchView batch,
    @NonNull VesselReferenceView vessel,
    @NonNull ProductionCurrentProfileView currentProfile,
    @NonNull PotentialAbvPrediction potentialAbv
) {

  public ProductionBatchDetailsView {
    requireNonNull(batch, "production batch view cannot be null");
    requireNonNull(vessel, "production vessel reference cannot be null");
    if (!batch.vesselId().equals(vessel.vesselId())) {
      throw new ApplicationValidationException(
          "production vessel reference must match batch vessel id"
      );
    }
    requireNonNull(currentProfile, "production current profile cannot be null");
    requireNonNull(potentialAbv, "production potential ABV prediction cannot be null");
  }
}
