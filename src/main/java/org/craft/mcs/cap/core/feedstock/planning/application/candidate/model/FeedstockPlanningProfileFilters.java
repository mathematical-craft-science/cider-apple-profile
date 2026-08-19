package org.craft.mcs.cap.core.feedstock.planning.application.candidate.model;

import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.jspecify.annotations.Nullable;

/** Planning-owned filters for selecting state-aware Feedstock candidates. */
public record FeedstockPlanningProfileFilters(
    @Nullable FeedstockPlanningBatchCriteria batch,
    @Nullable FeedstockSeason season
) {
}
