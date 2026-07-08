package org.craft.mcs.cap.core.profileanalysis.abv.domain;

import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FinalGravityPrediction(
    @NonNull SpecificGravityValue finalGravity,
    @NonNull FinalGravityPredictionSource source,
    @NonNull PredictionConfidence confidence
) {

  public FinalGravityPrediction {
    requireNonNull(finalGravity, "final gravity cannot be null");
    requireNonNull(source, "final gravity prediction source cannot be null");
    requireNonNull(confidence, "prediction confidence cannot be null");
  }
}
