package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Potential ABV assessment derived from the current feedstock profile sugar evidence.
 */
public record FeedstockCurrentProfilePotentialAbvView(
    @NonNull AxisValueKind originalGravityValueKind,
    @NonNull PotentialAbvPrediction prediction
) {

  public FeedstockCurrentProfilePotentialAbvView {
    requireNonNull(originalGravityValueKind, "original gravity value kind cannot be null");
    requireNonNull(prediction, "potential ABV prediction cannot be null");
  }
}
