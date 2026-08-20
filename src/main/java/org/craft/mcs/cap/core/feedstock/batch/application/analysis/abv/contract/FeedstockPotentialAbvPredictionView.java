package org.craft.mcs.cap.core.feedstock.batch.application.analysis.abv.contract;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Feedstock-owned potential ABV facts returned by the analysis boundary. */
public record FeedstockPotentialAbvPredictionView(
    @NonNull SpecificGravityValue originalGravity,
    @NonNull SpecificGravityValue predictedFinalGravity,
    @NonNull FinalGravitySource predictedFinalGravitySource,
    @NonNull AbvValue potentialAbv,
    @NonNull Method method,
    @NonNull Confidence confidence
) {

  public FeedstockPotentialAbvPredictionView {
    requireNonNull(originalGravity, "original gravity cannot be null");
    requireNonNull(predictedFinalGravity, "predicted final gravity cannot be null");
    requireNonNull(predictedFinalGravitySource, "predicted final gravity source cannot be null");
    requireNonNull(potentialAbv, "potential ABV cannot be null");
    requireNonNull(method, "ABV prediction method cannot be null");
    requireNonNull(confidence, "prediction confidence cannot be null");
  }

  public enum FinalGravitySource {
    FIXED_DRY_CIDER_DEFAULT,
    YEAST_CHARACTERISTICS,
    FERMENTATION_MODEL,
    FERMENTATION_MEASUREMENTS
  }

  public enum Method {
    STANDARD_GRAVITY_DROP
  }

  public enum Confidence {
    LOW,
    MEDIUM,
    HIGH
  }
}
