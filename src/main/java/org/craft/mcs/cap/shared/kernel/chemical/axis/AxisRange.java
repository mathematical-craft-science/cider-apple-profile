package org.craft.mcs.cap.shared.kernel.chemical.axis;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isAtLeast;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isLessThan;


/**
 * Unit-neutral, optionally unbounded decimal predicate used by classification rule tables.
 *
 * <p>The owning classification characteristic supplies the meaning and unit. Graph and target
 * geometry use typed measured-value ranges instead.</p>
 */
public record AxisRange(
    @Nullable BigDecimal minInclusive,
    @Nullable BigDecimal maxExclusive
) {

  public AxisRange {
    if (minInclusive != null
        && maxExclusive != null
        && minInclusive.compareTo(maxExclusive) >= 0) {
      throw new DomainRuleViolationException(
          "axis range minimum must be less than its maximum"
      );
    }
  }

  public static @NonNull AxisRange all() {
    return new AxisRange(null, null);
  }

  public static @NonNull AxisRange lessThan(@NonNull String maxExclusive) {
    return new AxisRange(null, new BigDecimal(requireNonNull(maxExclusive)));
  }

  public static @NonNull AxisRange atLeast(@NonNull String minInclusive) {
    return new AxisRange(new BigDecimal(requireNonNull(minInclusive)), null);
  }

  public static @NonNull AxisRange between(
      @NonNull String minInclusive,
      @NonNull String maxExclusive
  ) {
    return new AxisRange(
        new BigDecimal(requireNonNull(minInclusive)),
        new BigDecimal(requireNonNull(maxExclusive))
    );
  }

  public boolean contains(@NonNull BigDecimal value) {
    requireNonNull(value, "axis value cannot be null");

    return (minInclusive == null || isAtLeast(value, minInclusive))
        && (maxExclusive == null || isLessThan(value, maxExclusive));
  }

  public @Nullable BigDecimal lowerBoundary() {
    return minInclusive;
  }

  public @Nullable BigDecimal upperBoundary() {
    return maxExclusive;
  }
}
