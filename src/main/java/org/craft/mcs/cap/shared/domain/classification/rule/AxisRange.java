package org.craft.mcs.cap.shared.domain.classification.rule;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record AxisRange(
    @Nullable BigDecimal minInclusive,
    @Nullable BigDecimal maxExclusive
) {

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

    return (minInclusive == null || value.compareTo(minInclusive) >= 0)
        && (maxExclusive == null || value.compareTo(maxExclusive) < 0);
  }

  public @Nullable BigDecimal lowerBoundary() {
    return minInclusive;
  }

  public @Nullable BigDecimal upperBoundary() {
    return maxExclusive;
  }
}
