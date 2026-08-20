package org.craft.mcs.cap.core.formulation.calculation.application.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isPositive;


/** Target and explicit scoring configuration used by Formulation orchestration. */
public record FormulationTargetDefinition(
    @NonNull TargetArea sugarAcidity,
    @Nullable MeasuredValueRange<TanninValue> tannin,
    @NonNull ScoringConfiguration scoring
) {

  public FormulationTargetDefinition {
    requireNonNull(sugarAcidity, "formulation SG/TA target cannot be null");
    requireNonNull(scoring, "formulation target scoring cannot be null");
    if (tannin != null && tannin.upperBound() != MeasuredValueRange.UpperBound.INCLUSIVE) {
      throw new ApplicationValidationException("formulation tannin target must include its maximum");
    }
  }

  public sealed interface TargetArea permits PredefinedTarget, CustomRectangleTarget {
  }

  public record PredefinedTarget(@NonNull Zone zone) implements TargetArea {

    public PredefinedTarget {
      requireNonNull(zone, "predefined formulation target zone cannot be null");
    }
  }

  public record CustomRectangleTarget(
      @NonNull MeasuredValueRange<SpecificGravityValue> specificGravity,
      @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidity
  ) implements TargetArea {

    public CustomRectangleTarget {
      requireNonNull(specificGravity, "formulation SG target range cannot be null");
      requireNonNull(titratableAcidity, "formulation TA target range cannot be null");
    }
  }

  public record ScoringConfiguration(
      @NonNull Normalizer normalizer,
      @NonNull ObjectiveFunction objectiveFunction,
      @NonNull Weights weights
  ) {

    public ScoringConfiguration {
      requireNonNull(normalizer, "formulation target normalizer cannot be null");
      requireNonNull(objectiveFunction, "formulation target objective function cannot be null");
      requireNonNull(weights, "formulation target weights cannot be null");
    }
  }

  public record Weights(
      @NonNull BigDecimal specificGravity,
      @NonNull BigDecimal titratableAcidity,
      @Nullable BigDecimal tannin
  ) {

    public Weights {
      requireNonNull(specificGravity, "formulation SG target weight cannot be null");
      requireNonNull(titratableAcidity, "formulation TA target weight cannot be null");
      if (!isPositive(specificGravity) || !isPositive(titratableAcidity)
          || tannin != null && !isPositive(tannin)) {
        throw new ApplicationValidationException("formulation target weights must be positive");
      }
    }
  }

  public enum Zone {
    PERFECT_APPLES,
    HIGH_SUGAR_APPLES,
    LOW_ACIDITY_APPLES,
    MEDIUM_SUGAR_APPLES,
    LOW_SUGAR_APPLES
  }

  public enum Normalizer { MIN_MAX, EFFECTIVE_RANGE, THRESHOLD_SEGMENT, SENSITIVITY_UNIT }
  public enum ObjectiveFunction { DISTANCE_TO_TARGET_AREA, MANHATTAN_DISTANCE_TO_TARGET_AREA }
}
