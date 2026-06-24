package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Freshness assessment for the latest measurement used by a planning row.
 *
 * <p>The current stale policy belongs to the read model, not to the domain aggregate. A future
 * configurable policy can replace this value without changing feedstock measurement invariants.
 */
public record FeedstockPlanningMeasurementFreshnessView(
    @NonNull FeedstockPlanningMeasurementFreshnessStatus status,
    boolean stale,
    @Nullable String reason
) {

  public FeedstockPlanningMeasurementFreshnessView {
    requireNonNull(status, "feedstock planning measurement freshness status cannot be null");
  }
}
