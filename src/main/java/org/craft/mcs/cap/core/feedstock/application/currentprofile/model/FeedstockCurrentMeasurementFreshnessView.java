package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Aggregate freshness assessment of evidence used by the current Feedstock profile.
 *
 * <p>The current stale policy belongs to the read model, not to the domain aggregate. A future
 * configurable policy can replace this value without changing feedstock measurement invariants.
 */
public record FeedstockCurrentMeasurementFreshnessView(
    @NonNull MeasurementFreshnessStatus status,
    boolean stale,
    @Nullable String reason
) {

  public FeedstockCurrentMeasurementFreshnessView {
    requireNonNull(status, "feedstock current measurement freshness status cannot be null");
  }
}
