package org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isPositive;


/** Published configuration that defines how target-fit distances are normalized and weighted. */
public record TargetFitScoringConfiguration(
    @NonNull Normalizer normalizer,
    @NonNull ObjectiveFunction objectiveFunction,
    @NonNull ObjectiveWeights weights
) {

  public TargetFitScoringConfiguration {
    requireNonNull(normalizer, "target normalizer cannot be null");
    requireNonNull(objectiveFunction, "target objective function cannot be null");
    requireNonNull(weights, "target objective weights cannot be null");
  }

  public record ObjectiveWeights(
      @NonNull BigDecimal specificGravity,
      @NonNull BigDecimal titratableAcidity,
      @Nullable BigDecimal tannin
  ) {

    public ObjectiveWeights {
      requireNonNull(specificGravity, "specific gravity weight cannot be null");
      requireNonNull(titratableAcidity, "titratable acidity weight cannot be null");
      if (!isPositive(specificGravity) || !isPositive(titratableAcidity)
          || tannin != null && !isPositive(tannin)) {
        throw new ApplicationValidationException("target weights must be positive");
      }
    }
  }

  public enum Normalizer {
    MIN_MAX,
    EFFECTIVE_RANGE,
    THRESHOLD_SEGMENT,
    SENSITIVITY_UNIT
  }

  public enum ObjectiveFunction {
    DISTANCE_TO_TARGET_AREA,
    MANHATTAN_DISTANCE_TO_TARGET_AREA
  }
}
