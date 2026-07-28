package org.craft.mcs.cap.shared.kernel.measurement;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Typed range of canonical values belonging to one measurement characteristic.
 *
 * <p>The lower bound is always inclusive. The upper-bound policy is explicit so classification
 * partitions can be half-open while user-defined target ranges remain closed.</p>
 */
public record MeasuredValueRange<V extends MeasuredValue>(
    @NonNull V min,
    @NonNull V max,
    @NonNull UpperBound upperBound
) {

  public MeasuredValueRange {
    requireNonNull(min, "measured value range minimum cannot be null");
    requireNonNull(max, "measured value range maximum cannot be null");
    requireNonNull(upperBound, "measured value range upper bound cannot be null");
    if (min.getClass() != max.getClass()) {
      throw new DomainRuleViolationException(
          "measured value range bounds must have the same measurement type"
      );
    }
    final var comparison = min.compareTo(max);
    if (comparison > 0 || (comparison == 0 && upperBound == UpperBound.EXCLUSIVE)) {
      throw new DomainRuleViolationException(
          "measured value range minimum must precede its maximum"
      );
    }
  }

  public static <V extends MeasuredValue> @NonNull MeasuredValueRange<V> closed(
      @NonNull V min,
      @NonNull V max
  ) {
    return new MeasuredValueRange<>(min, max, UpperBound.INCLUSIVE);
  }

  public static <V extends MeasuredValue> @NonNull MeasuredValueRange<V> closedOpen(
      @NonNull V min,
      @NonNull V max
  ) {
    return new MeasuredValueRange<>(min, max, UpperBound.EXCLUSIVE);
  }

  public boolean contains(@NonNull V value) {
    requireNonNull(value, "measured range value cannot be null");
    final var maximumComparison = value.compareTo(max);
    return value.compareTo(min) >= 0
        && (maximumComparison < 0
            || maximumComparison == 0 && upperBound == UpperBound.INCLUSIVE);
  }

  /**
   * Projects a value onto the range closure.
   *
   * <p>For a half-open range the exclusive maximum remains the distance boundary even though
   * {@link #contains(MeasuredValue)} does not treat it as a member.</p>
   */
  public @NonNull V projectOntoClosure(@NonNull V value) {
    requireNonNull(value, "measured range value cannot be null");
    if (value.compareTo(min) < 0) {
      return min;
    }
    if (value.compareTo(max) > 0) {
      return max;
    }
    return value;
  }

  /** Defines whether the maximum value belongs to the range. */
  public enum UpperBound {

    /** The maximum value belongs to the range. */
    INCLUSIVE,

    /** The maximum value is only the boundary of the range. */
    EXCLUSIVE
  }
}
