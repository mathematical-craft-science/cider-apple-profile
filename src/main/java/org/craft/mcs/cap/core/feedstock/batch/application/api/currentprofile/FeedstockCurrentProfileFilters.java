package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;

import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.jspecify.annotations.Nullable;


/**
 * Source-side filters for selecting FeedstockBatch analysis candidates.
 *
 * <p>These criteria only select business subjects. Derived graph, target, or composition criteria
 * belong to the concrete scenario query.</p>
 */
public record FeedstockCurrentProfileFilters(
    @Nullable FeedstockBatchCriteria batch,
    @Nullable FeedstockSeason season
) {
}
