package org.craft.mcs.cap.core.production.application.model;

import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Production batch read model enriched with derived analysis required by read scenarios. */
public record ProductionBatchDetailsView(
    @NonNull ProductionBatchView batch,
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull ProductionCurrentProfileView currentProfile,
    @NonNull PotentialAbvPrediction potentialAbv
) {

  public ProductionBatchDetailsView {
    requireNonNull(batch, "production batch view cannot be null");
    requireNonNull(currentMaterialStateId, "production current material state id cannot be null");
    requireNonNull(currentProfile, "production current profile cannot be null");
    requireNonNull(potentialAbv, "production potential ABV prediction cannot be null");
  }
}
