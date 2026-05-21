package org.craft.mcs.cap.shared.kernel.math;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Expressive comparison helpers for precise decimal domain values.
 *
 * <p>{@link BigDecimal#compareTo(BigDecimal)} is intentionally still the underlying comparison
 * primitive, but domain code should read in business terms such as "is positive", "is at most" or
 * "is inside range" instead of leaking numeric comparison codes.
 */
public final class DecimalComparisons {

  private static final BigDecimal ZERO = BigDecimal.ZERO;

  private DecimalComparisons() {
  }

  public static boolean isNegative(@NonNull BigDecimal value) {
    requireNonNull(value, "decimal value cannot be null");
    return value.compareTo(ZERO) < 0;
  }

  public static boolean isPositive(@NonNull BigDecimal value) {
    requireNonNull(value, "decimal value cannot be null");
    return value.compareTo(ZERO) > 0;
  }

  public static boolean isLessThan(
      @NonNull BigDecimal left,
      @NonNull BigDecimal right
  ) {
    requireNonNull(left, "left decimal value cannot be null");
    requireNonNull(right, "right decimal value cannot be null");
    return left.compareTo(right) < 0;
  }

  public static boolean isAtLeast(
      @NonNull BigDecimal left,
      @NonNull BigDecimal right
  ) {
    requireNonNull(left, "left decimal value cannot be null");
    requireNonNull(right, "right decimal value cannot be null");
    return left.compareTo(right) >= 0;
  }

  public static boolean isAtMost(
      @NonNull BigDecimal left,
      @NonNull BigDecimal right
  ) {
    requireNonNull(left, "left decimal value cannot be null");
    requireNonNull(right, "right decimal value cannot be null");
    return left.compareTo(right) <= 0;
  }

  public static boolean isGreaterThan(
      @NonNull BigDecimal left,
      @NonNull BigDecimal right
  ) {
    requireNonNull(left, "left decimal value cannot be null");
    requireNonNull(right, "right decimal value cannot be null");
    return left.compareTo(right) > 0;
  }

  public static boolean isBetweenInclusive(
      @NonNull BigDecimal value,
      @NonNull BigDecimal min,
      @NonNull BigDecimal max
  ) {
    requireNonNull(value, "decimal value cannot be null");
    requireNonNull(min, "minimum decimal value cannot be null");
    requireNonNull(max, "maximum decimal value cannot be null");
    return isAtLeast(value, min) && isAtMost(value, max);
  }
}
