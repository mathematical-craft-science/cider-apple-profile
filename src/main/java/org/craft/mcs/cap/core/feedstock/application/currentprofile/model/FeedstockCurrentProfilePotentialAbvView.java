package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Potential ABV assessment derived from the current feedstock profile sugar evidence.
 */
public record FeedstockCurrentProfilePotentialAbvView(
    @NonNull CharacteristicValue<SpecificGravityValue> originalGravity,
    @NonNull PotentialAbvPrediction prediction
) {

  public FeedstockCurrentProfilePotentialAbvView {
    requireNonNull(originalGravity, "original gravity cannot be null");
    requireNonNull(prediction, "potential ABV prediction cannot be null");
  }
}
