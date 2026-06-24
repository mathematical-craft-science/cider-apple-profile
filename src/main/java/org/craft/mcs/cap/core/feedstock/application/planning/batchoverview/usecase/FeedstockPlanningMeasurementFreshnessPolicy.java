package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.usecase;

import java.time.Duration;
import java.time.Instant;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Configurable policy that defines when a latest feedstock measurement becomes stale for planning.
 *
 * <p>The policy belongs to the feedstock planning read side. It does not change measurement
 * invariants; it only expresses how fresh evidence must be for planning-oriented assessments.
 */
public record FeedstockPlanningMeasurementFreshnessPolicy(
    @NonNull Duration staleAfter
) {

  private static final @NonNull Duration DEFAULT_STALE_AFTER = Duration.ofDays(14);

  public FeedstockPlanningMeasurementFreshnessPolicy {
    requireNonNull(staleAfter, "feedstock planning measurement stale duration cannot be null");
    if (staleAfter.isZero() || staleAfter.isNegative()) {
      throw new IllegalArgumentException("feedstock planning measurement stale duration must be positive");
    }
  }

  public static @NonNull FeedstockPlanningMeasurementFreshnessPolicy defaultPolicy() {
    return new FeedstockPlanningMeasurementFreshnessPolicy(DEFAULT_STALE_AFTER);
  }

  public boolean isStale(
      @NonNull Instant measuredAt,
      @NonNull Instant now
  ) {
    requireNonNull(measuredAt, "feedstock measurement timestamp cannot be null");
    requireNonNull(now, "current timestamp cannot be null");
    return !now.isBefore(measuredAt.plus(staleAfter));
  }
}
