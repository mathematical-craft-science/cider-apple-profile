package org.craft.mcs.cap.core.feedstock.application.detail.port.in;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.FeedstockMeasurementSummary;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Application read model for feedstock batch details.
 *
 * <p>The view composes aggregate metadata, measurement history and current-profile analysis without
 * duplicating feedstock batch or measurement lifecycle behavior.
 */
public record FeedstockBatchDetailView(
    @NonNull FeedstockBatchSummary batch,
    @NonNull FeedstockBatchCurrentProfileView currentProfile,
    @NonNull List<@NonNull FeedstockMeasurementSummary> measurements
) {

  public FeedstockBatchDetailView {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    measurements = List.copyOf(requireNonNull(measurements, "feedstock measurements cannot be null"));
  }
}
