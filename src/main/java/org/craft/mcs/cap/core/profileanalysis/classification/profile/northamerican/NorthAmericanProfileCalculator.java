package org.craft.mcs.cap.core.profileanalysis.classification.profile.northamerican;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Calculates the North American cider profile levels from canonical chemical axes.
 *
 * <p>The calculator requires sugar, acidity and tannin. It has no partial-result semantics by
 * design: application services must check {@code NORTH_AMERICAN_PROFILE} capability before calling
 * it and decide how missing axes should be reported to a user. The calculation itself is a
 * threshold lookup over configured low/medium/high ranges and returns the three-level semantic
 * profile used by downstream classification and future single-variety assessment scenarios.
 */
public final class NorthAmericanProfileCalculator {

  private final @NonNull NorthAmericanThresholds thresholds;

  public NorthAmericanProfileCalculator(@NonNull NorthAmericanThresholds thresholds) {
    this.thresholds = requireNonNull(thresholds);
  }

  private static @NonNull NorthAmericanLevel levelOf(
      @NonNull BigDecimal value,
      @NonNull AxisRange low,
      @NonNull AxisRange medium,
      @NonNull AxisRange high,
      @Nullable AxisRange veryHigh
  ) {
    requireNonNull(value, "axis value cannot be null");
    requireNonNull(low, "low range cannot be null");
    requireNonNull(medium, "medium range cannot be null");
    requireNonNull(high, "high range cannot be null");

    if (low.contains(value)) {
      return NorthAmericanLevel.LOW;
    }
    if (medium.contains(value)) {
      return NorthAmericanLevel.MEDIUM;
    }
    if (high.contains(value)) {
      return NorthAmericanLevel.HIGH;
    }
    if (veryHigh != null && veryHigh.contains(value)) {
      return NorthAmericanLevel.VERY_HIGH;
    }

    throw new DomainRuleViolationException("value does not fit any North American profile range: " + value);
  }

  private static @NonNull BigDecimal axisValue(
      @NonNull ChemicalProfileAxes axes,
      @NonNull ChemicalAxis axis
  ) {
    return axes.valueOf(axis)
        .orElseThrow(() -> new DomainRuleViolationException("missing required axis: " + axis))
        .value();
  }

  public @NonNull NorthAmericanProfile calculate(@NonNull ChemicalProfileAxes axes) {
    requireNonNull(axes, "chemical profile axes cannot be null");

    return new NorthAmericanProfile(
        levelOf(
            axisValue(axes, ChemicalAxis.SUGAR),
            thresholds.lowSugar(),
            thresholds.mediumSugar(),
            thresholds.highSugar(),
            thresholds.veryHighSugar()
        ),
        levelOf(
            axisValue(axes, ChemicalAxis.ACIDITY),
            thresholds.lowAcidity(),
            thresholds.mediumAcidity(),
            thresholds.highAcidity(),
            thresholds.veryHighAcidity()
        ),
        levelOf(
            axisValue(axes, ChemicalAxis.TANNINITY),
            thresholds.lowTannin(),
            thresholds.mediumTannin(),
            thresholds.highTannin(),
            null
        )
    );
  }
}
