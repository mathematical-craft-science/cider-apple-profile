package org.craft.mcs.cap.shared.application.measurement;

import java.time.Duration;
import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Applies a configured stale threshold consistently to immutable measurement evidence. */
public record MeasurementFreshnessPolicy(@NonNull Duration staleAfter) {

  public MeasurementFreshnessPolicy {
    requireNonNull(staleAfter, "measurement stale duration cannot be null");
    if (staleAfter.isZero() || staleAfter.isNegative()) {
      throw new DomainRuleViolationException("measurement stale duration must be positive");
    }
  }

  public @NonNull MeasurementFreshnessStatus status(
      @NonNull Instant measuredAt,
      @NonNull Instant assessedAt
  ) {
    requireNonNull(measuredAt, "measurement timestamp cannot be null");
    requireNonNull(assessedAt, "measurement assessment timestamp cannot be null");
    return assessedAt.isBefore(measuredAt.plus(staleAfter))
        ? MeasurementFreshnessStatus.CURRENT
        : MeasurementFreshnessStatus.STALE;
  }

  public boolean isStale(
      @NonNull Instant measuredAt,
      @NonNull Instant assessedAt
  ) {
    return status(measuredAt, assessedAt) == MeasurementFreshnessStatus.STALE;
  }
}
