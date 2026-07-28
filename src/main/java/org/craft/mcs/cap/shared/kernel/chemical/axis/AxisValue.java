package org.craft.mcs.cap.shared.kernel.chemical.axis;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/** Immutable scalar projection of one chemical characteristic onto an analysis axis. */
public record AxisValue(
    @NonNull ChemicalAxis axis,
    @NonNull BigDecimal value,
    @NonNull AxisValueKind valueKind,
    @Nullable BigDecimal min,
    @Nullable BigDecimal max
) {

  public AxisValue {
    requireNonNull(axis, "chemical axis cannot be null");
    requireNonNull(value, "axis value cannot be null");
    requireNonNull(valueKind, "axis value kind cannot be null");
    validateShape(value, valueKind, min, max);
  }

  /** Creates an axis value backed by one direct point characteristic. */
  public static @NonNull AxisValue point(
      @NonNull ChemicalAxis axis,
      @NonNull BigDecimal value
  ) {
    return new AxisValue(axis, value, AxisValueKind.POINT, null, null);
  }

  /** Creates an axis value calculated from several source values. */
  public static @NonNull AxisValue calculated(
      @NonNull ChemicalAxis axis,
      @NonNull BigDecimal value
  ) {
    return new AxisValue(axis, value, AxisValueKind.CALCULATED, null, null);
  }

  /** Creates an interval projection whose scalar coordinate is the exact interval midpoint. */
  public static @NonNull AxisValue intervalMidpoint(
      @NonNull ChemicalAxis axis,
      @NonNull BigDecimal min,
      @NonNull BigDecimal max
  ) {
    requireNonNull(min, "axis interval minimum cannot be null");
    requireNonNull(max, "axis interval maximum cannot be null");
    return new AxisValue(
        axis,
        midpoint(min, max),
        AxisValueKind.INTERVAL_MIDPOINT,
        min,
        max
    );
  }

  private static void validateShape(
      @NonNull BigDecimal value,
      @NonNull AxisValueKind valueKind,
      @Nullable BigDecimal min,
      @Nullable BigDecimal max
  ) {
    switch (valueKind) {
      case POINT, CALCULATED -> requireAbsentBounds(min, max);
      case INTERVAL_MIDPOINT -> requireValidInterval(value, min, max);
    }
  }

  private static void requireAbsentBounds(
      @Nullable BigDecimal min,
      @Nullable BigDecimal max
  ) {
    if (min != null || max != null) {
      throw new DomainRuleViolationException("point and calculated axis values cannot declare interval bounds");
    }
  }

  private static void requireValidInterval(
      @NonNull BigDecimal value,
      @Nullable BigDecimal min,
      @Nullable BigDecimal max
  ) {
    if (min == null || max == null) {
      throw new DomainRuleViolationException("interval midpoint axis value requires both bounds");
    }
    if (min.compareTo(max) > 0) {
      throw new DomainRuleViolationException("axis interval minimum cannot exceed maximum");
    }
    if (value.compareTo(midpoint(min, max)) != 0) {
      throw new DomainRuleViolationException("axis interval value must equal the interval midpoint");
    }
  }
}
