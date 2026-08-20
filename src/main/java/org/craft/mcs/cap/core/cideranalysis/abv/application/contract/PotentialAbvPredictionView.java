package org.craft.mcs.cap.core.cideranalysis.abv.application.contract;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Published explainable result of potential ABV prediction. */
public record PotentialAbvPredictionView(
    @NonNull SpecificGravityValue originalGravity,
    @NonNull SpecificGravityValue predictedFinalGravity,
    @NonNull FinalGravityPredictionSource predictedFinalGravitySource,
    @NonNull AbvValue potentialAbv,
    @NonNull AbvPredictionMethod method,
    @NonNull PredictionConfidence confidence
) {

  public PotentialAbvPredictionView {
    requireNonNull(originalGravity, "original gravity cannot be null");
    requireNonNull(predictedFinalGravity, "predicted final gravity cannot be null");
    requireNonNull(predictedFinalGravitySource, "predicted final gravity source cannot be null");
    requireNonNull(potentialAbv, "potential ABV cannot be null");
    requireNonNull(method, "ABV prediction method cannot be null");
    requireNonNull(confidence, "prediction confidence cannot be null");
    if (predictedFinalGravity.exceeds(originalGravity)) {
      throw new ApplicationValidationException("predicted final gravity must not exceed original gravity");
    }
  }
}
