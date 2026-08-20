package org.craft.mcs.cap.core.feedstock.batch.application.detail.contract;

import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Application read model for feedstock batch details.
 *
 * <p>The view composes aggregate metadata and current-profile analysis without duplicating
 * measurement history owned by the generic Measurement context.
 */
public record FeedstockBatchDetailView(
    @NonNull FeedstockBatchView batch,
    @NonNull FeedstockBatchCurrentProfileView currentProfile
) {

  public FeedstockBatchDetailView {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
  }
}
