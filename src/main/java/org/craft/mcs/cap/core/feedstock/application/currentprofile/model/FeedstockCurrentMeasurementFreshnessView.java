package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Freshness assessment for the latest measurement used as the current feedstock profile.
 *
 * <p>The current stale policy belongs to the read model, not to the domain aggregate. A future
 * configurable policy can replace this value without changing feedstock measurement invariants.
 */
public record FeedstockCurrentMeasurementFreshnessView(
    @NonNull FeedstockCurrentMeasurementFreshnessStatus status,
    boolean stale,
    @Nullable String reason
) {

  public FeedstockCurrentMeasurementFreshnessView {
    requireNonNull(status, "feedstock current measurement freshness status cannot be null");
  }
}
