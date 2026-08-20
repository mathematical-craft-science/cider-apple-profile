package org.craft.mcs.cap.core.production.planning.application.contract;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Immutable potential-ABV facts saved with a production candidate. */
public record ProductionCandidatePotentialAbvView(
    @NonNull SpecificGravityValue originalGravity,
    @NonNull SpecificGravityValue predictedFinalGravity,
    @NonNull FinalGravitySource predictedFinalGravitySource,
    @NonNull AbvValue potentialAbv,
    @NonNull Method method,
    @NonNull Confidence confidence
) {

  public ProductionCandidatePotentialAbvView {
    requireNonNull(originalGravity, "production candidate original gravity cannot be null");
    requireNonNull(predictedFinalGravity, "production candidate predicted final gravity cannot be null");
    requireNonNull(predictedFinalGravitySource, "production candidate final gravity source cannot be null");
    requireNonNull(potentialAbv, "production candidate potential ABV cannot be null");
    requireNonNull(method, "production candidate ABV method cannot be null");
    requireNonNull(confidence, "production candidate ABV confidence cannot be null");
    if (predictedFinalGravity.exceeds(originalGravity)) {
      throw new ApplicationValidationException(
          "production candidate predicted final gravity must not exceed original gravity"
      );
    }
  }

  public enum FinalGravitySource {
    FIXED_DRY_CIDER_DEFAULT,
    YEAST_CHARACTERISTICS,
    FERMENTATION_MODEL,
    FERMENTATION_MEASUREMENTS
  }
  public enum Method { STANDARD_GRAVITY_DROP }
  public enum Confidence { LOW, MEDIUM, HIGH }
}
