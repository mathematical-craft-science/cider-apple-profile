package org.craft.mcs.cap.core.feedstock.application.detail.model;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
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
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull FeedstockBatchCurrentProfileView currentProfile,
    @NonNull List<@NonNull FeedstockMeasurementSummary> measurements
) {

  public FeedstockBatchDetailView {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    requireNonNull(currentMaterialStateId, "feedstock current material state id cannot be null");
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    measurements = List.copyOf(requireNonNull(measurements, "feedstock measurements cannot be null"));
  }
}
