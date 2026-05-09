package org.craft.mcs.cap.shared.domain.fermentation;

import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record PotentialAbvPrediction(
    @NonNull SpecificGravityValue originalGravity,
    @NonNull SpecificGravityValue assumedFinalGravity,
    @NonNull FinalGravityPredictionSource assumedFinalGravitySource,
    @NonNull AbvValue potentialAbv,
    @NonNull AbvPredictionMethod method,
    @NonNull PredictionConfidence confidence
) {

  public PotentialAbvPrediction {
    requireNonNull(originalGravity, "original gravity cannot be null");
    requireNonNull(assumedFinalGravity, "assumed final gravity cannot be null");
    requireNonNull(assumedFinalGravitySource, "assumed final gravity source cannot be null");
    requireNonNull(potentialAbv, "potential ABV cannot be null");
    requireNonNull(method, "ABV prediction method cannot be null");
    requireNonNull(confidence, "prediction confidence cannot be null");
  }
}
