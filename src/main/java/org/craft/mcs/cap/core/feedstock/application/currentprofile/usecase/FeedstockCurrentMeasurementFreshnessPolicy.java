package org.craft.mcs.cap.core.feedstock.application.currentprofile.usecase;

import java.time.Duration;
import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Configurable policy that defines when a latest feedstock measurement becomes stale.
 *
 * <p>The policy belongs to the current-profile read side. It does not change measurement
 * invariants; it only expresses how fresh evidence must be for read-side assessments.
 */
public record FeedstockCurrentMeasurementFreshnessPolicy(
    @NonNull Duration staleAfter
) {

  public FeedstockCurrentMeasurementFreshnessPolicy {
    requireNonNull(staleAfter, "feedstock current measurement stale duration cannot be null");
    if (staleAfter.isZero() || staleAfter.isNegative()) {
      throw new DomainRuleViolationException("feedstock current measurement stale duration must be positive");
    }
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
