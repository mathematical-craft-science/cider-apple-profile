package org.craft.mcs.cap.core.cideranalysis.abv.domain;

import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Explainable potential ABV prediction result.
 *
 * <p>The result keeps the original gravity, predicted final gravity, source of that FG assumption,
 * prediction method and confidence. This lets API consumers distinguish a low-confidence fixed FG
 * estimate from future yeast-aware or fermentation-model predictions.
 */
public record PotentialAbvPrediction(
    @NonNull SpecificGravityValue originalGravity,
    @NonNull SpecificGravityValue predictedFinalGravity,
    @NonNull FinalGravityPredictionSource predictedFinalGravitySource,
    @NonNull AbvValue potentialAbv,
    @NonNull AbvPredictionMethod method,
    @NonNull PredictionConfidence confidence
) {

  public PotentialAbvPrediction {
    requireNonNull(originalGravity, "original gravity cannot be null");
    requireNonNull(predictedFinalGravity, "predicted final gravity cannot be null");
    requireNonNull(predictedFinalGravitySource, "predicted final gravity source cannot be null");
    requireNonNull(potentialAbv, "potential ABV cannot be null");
    requireNonNull(method, "ABV prediction method cannot be null");
    requireNonNull(confidence, "prediction confidence cannot be null");
    if (predictedFinalGravity.exceeds(originalGravity)) {
      throw new DomainRuleViolationException("predicted final gravity must not exceed original gravity");
    }
  }
}
