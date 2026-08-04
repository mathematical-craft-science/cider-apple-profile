package org.craft.mcs.cap.shared.kernel.measurement;

import org.jspecify.annotations.NonNull;

/**
 * Position of a measured value relative to a range's membership rules.
 *
 * <p>An exclusive upper bound is {@link #ABOVE} when the compared value equals that bound, even
 * though its numeric distance to the range closure is zero.</p>
 */
public enum MeasuredValueRangePosition {

  /** The value precedes the inclusive lower bound. */
  BELOW,

  /** The value belongs to the range according to both bound policies. */
  WITHIN,

  /** The value follows the range or equals its exclusive upper bound. */
  ABOVE;

  /** Converts a standard comparison result into a position relative to the compared target. */
  public static @NonNull MeasuredValueRangePosition fromComparison(int comparison) {
    if (comparison < 0) {
      return BELOW;
    }
    if (comparison > 0) {
      return ABOVE;
    }
    return WITHIN;
  }
}
