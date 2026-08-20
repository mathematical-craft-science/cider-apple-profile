package org.craft.mcs.cap.core.production.planning.application.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isPositive;


/** Production-owned settings for calculating an authoritative candidate snapshot. */
public record ProductionBatchCandidateCalculationRequest(
    @NonNull TanninPolicy tanninPolicy,
    @Nullable TargetDefinition target,
    @NonNull Calculator calculator
) {

  public ProductionBatchCandidateCalculationRequest {
    requireNonNull(tanninPolicy, "production candidate tannin policy cannot be null");
    requireNonNull(calculator, "production candidate calculator cannot be null");
  }

  public record TargetDefinition(
      @NonNull TargetArea sugarAcidity,
      @Nullable MeasuredValueRange<TanninValue> tannin,
      @NonNull Scoring scoring
  ) {

    public TargetDefinition {
      requireNonNull(sugarAcidity, "production candidate SG/TA target cannot be null");
      requireNonNull(scoring, "production candidate target scoring cannot be null");
      if (tannin != null && tannin.upperBound() != MeasuredValueRange.UpperBound.INCLUSIVE) {
        throw new ApplicationValidationException("production candidate tannin target must include its maximum");
      }
    }
  }

  public sealed interface TargetArea permits PredefinedTarget, CustomRectangleTarget {
  }

  public record PredefinedTarget(@NonNull Zone zone) implements TargetArea {
    public PredefinedTarget {
      requireNonNull(zone, "production candidate target zone cannot be null");
    }
  }

  public record CustomRectangleTarget(
      @NonNull MeasuredValueRange<SpecificGravityValue> specificGravity,
      @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidity
  ) implements TargetArea {

    public CustomRectangleTarget {
      requireNonNull(specificGravity, "production candidate SG target range cannot be null");
      requireNonNull(titratableAcidity, "production candidate TA target range cannot be null");
    }
  }

  public record Scoring(
      @NonNull ProductionCandidateNormalizer normalizer,
      @NonNull ObjectiveFunction objectiveFunction,
      @NonNull Weights weights
  ) {

    public Scoring {
      requireNonNull(normalizer, "production candidate target normalizer cannot be null");
      requireNonNull(objectiveFunction, "production candidate target objective function cannot be null");
      requireNonNull(weights, "production candidate target weights cannot be null");
    }
  }

  public record Weights(
      @NonNull BigDecimal specificGravity,
      @NonNull BigDecimal titratableAcidity,
      @Nullable BigDecimal tannin
  ) {

    public Weights {
      requireNonNull(specificGravity, "production candidate SG weight cannot be null");
      requireNonNull(titratableAcidity, "production candidate TA weight cannot be null");
      if (!isPositive(specificGravity) || !isPositive(titratableAcidity)
          || tannin != null && !isPositive(tannin)) {
        throw new ApplicationValidationException("production candidate target weights must be positive");
      }
    }
  }

  public enum TanninPolicy { CALCULATE_WHEN_COMPLETE, IGNORE_TANNIN }
  public enum Calculator { MASS_BALANCE }
  public enum ObjectiveFunction { DISTANCE_TO_TARGET_AREA, MANHATTAN_DISTANCE_TO_TARGET_AREA }
  public enum Zone {
    PERFECT_APPLES,
    HIGH_SUGAR_APPLES,
    LOW_ACIDITY_APPLES,
    MEDIUM_SUGAR_APPLES,
    LOW_SUGAR_APPLES
  }
}
