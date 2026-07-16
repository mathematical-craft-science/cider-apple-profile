package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * Converts an unbounded technical distance into a stable bounded target-fit assessment.
 *
 * <p>The reciprocal mapping preserves distance ordering and supports every configured normalizer and
 * metric without assuming that distance is limited to {@code 0..1}. The resulting percentage is
 * meaningful only within the scoring configuration that produced the distance.
 */
public final class ProfileTargetFitPolicy {

  private static final int EXCELLENT_MATCH_PERCENT = 90;
  private static final int CLOSE_MATCH_PERCENT = 70;
  private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

  private ProfileTargetFitPolicy() {
  }

  public static @NonNull ProfileTargetFitAssessment assess(
      @NonNull ProfileTargetDistance distance
  ) {
    requireNonNull(distance, "profile target distance cannot be null");
    if (!distance.scored()) {
      return ProfileTargetFitAssessment.unscorable();
    }

    final var matchPercent = distance.insideTarget()
        ? 100
        : matchPercent(requireNonNull(distance.value(), "scored profile target distance value cannot be null"));
    return ProfileTargetFitAssessment.scored(matchPercent);
  }

  private static int matchPercent(@NonNull BigDecimal distance) {
    return ONE_HUNDRED
        .divide(BigDecimal.ONE.add(distance), 0, RoundingMode.HALF_UP)
        .intValueExact();
  }

  static @NonNull ProfileTargetFitStatus statusFor(int matchPercent) {
    if (matchPercent >= EXCELLENT_MATCH_PERCENT) {
      return ProfileTargetFitStatus.EXCELLENT;
    }
    if (matchPercent >= CLOSE_MATCH_PERCENT) {
      return ProfileTargetFitStatus.CLOSE;
    }
    return ProfileTargetFitStatus.NEEDS_ADJUSTMENT;
  }
}
