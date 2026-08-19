package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;

import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Aggregate freshness assessment of evidence used by the current Feedstock profile.
 *
 * <p>Per-characteristic freshness is calculated by the Measurement context using the applicable
 * workspace policy. This view only summarizes the evidence relevant to Feedstock planning.
 */
public record FeedstockCurrentProfileFreshnessView(
    @NonNull MeasurementFreshnessStatus status,
    @Nullable String reason
) {

  public FeedstockCurrentProfileFreshnessView {
    requireNonNull(status, "feedstock current profile freshness status cannot be null");
  }

  public boolean stale() {
    return status == MeasurementFreshnessStatus.STALE;
  }
}
