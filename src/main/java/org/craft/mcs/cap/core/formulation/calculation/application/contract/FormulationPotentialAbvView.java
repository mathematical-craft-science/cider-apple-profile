package org.craft.mcs.cap.core.formulation.calculation.application.contract;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Potential ABV facts owned by a Formulation result. */
public record FormulationPotentialAbvView(
    @NonNull SpecificGravityValue originalGravity,
    @NonNull SpecificGravityValue predictedFinalGravity,
    @NonNull FinalGravitySource predictedFinalGravitySource,
    @NonNull AbvValue potentialAbv,
    @NonNull Method method,
    @NonNull Confidence confidence
) {

  public FormulationPotentialAbvView {
    requireNonNull(originalGravity, "formulation original gravity cannot be null");
    requireNonNull(predictedFinalGravity, "formulation predicted final gravity cannot be null");
    requireNonNull(predictedFinalGravitySource, "formulation final gravity source cannot be null");
    requireNonNull(potentialAbv, "formulation potential ABV cannot be null");
    requireNonNull(method, "formulation ABV method cannot be null");
    requireNonNull(confidence, "formulation ABV confidence cannot be null");
    if (predictedFinalGravity.exceeds(originalGravity)) {
      throw new ApplicationValidationException(
          "formulation predicted final gravity must not exceed original gravity"
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
