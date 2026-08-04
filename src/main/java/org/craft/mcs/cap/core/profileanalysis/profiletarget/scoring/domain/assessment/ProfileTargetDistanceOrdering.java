package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Orders target assessments by scoreability, technical distance and authoritative target membership.
 *
 * <p>Membership resolves the zero-distance tie created by an excluded boundary of a half-open target.
 * A point inside the target precedes a point that only belongs to the target's closure.</p>
 */
public final class ProfileTargetDistanceOrdering {

  private ProfileTargetDistanceOrdering() {
  }

  public static int compare(
      @NonNull ProfileTargetDistance left,
      @NonNull ProfileTargetDistance right
  ) {
    requireNonNull(left, "left profile target distance cannot be null");
    requireNonNull(right, "right profile target distance cannot be null");
    final var distanceComparison = compareValues(left, right);
    return distanceComparison != 0
        ? distanceComparison
        : Boolean.compare(right.insideTarget(), left.insideTarget());
  }

  private static int compareValues(
      @NonNull ProfileTargetDistance left,
      @NonNull ProfileTargetDistance right
  ) {
    final var leftValue = left.value();
    final var rightValue = right.value();
    if (leftValue == null && rightValue == null) {
      return 0;
    }
    if (leftValue == null) {
      return 1;
    }
    if (rightValue == null) {
      return -1;
    }
    return leftValue.compareTo(rightValue);
  }
}
