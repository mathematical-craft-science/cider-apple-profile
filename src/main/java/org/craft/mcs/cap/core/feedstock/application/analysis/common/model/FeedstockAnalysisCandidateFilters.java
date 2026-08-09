package org.craft.mcs.cap.core.feedstock.application.analysis.common.model;

import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.jspecify.annotations.Nullable;


/**
 * Source-side filters for selecting FeedstockBatch analysis candidates.
 *
 * <p>These criteria only select business subjects. Derived graph, target, or composition criteria
 * belong to the concrete scenario query.</p>
 */
public record FeedstockAnalysisCandidateFilters(
    @Nullable FeedstockBatchCriteria batch,
    @Nullable FeedstockSeason season
) {
}
