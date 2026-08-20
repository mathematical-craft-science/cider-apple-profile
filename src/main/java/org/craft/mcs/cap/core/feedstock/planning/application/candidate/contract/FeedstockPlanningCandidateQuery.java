package org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract;

import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Planning-owned selection criteria for state-aware Feedstock candidates. */
public record FeedstockPlanningCandidateQuery(
    @NonNull WorkspaceId workspaceId,
    @Nullable FeedstockPlanningBatchCriteria batch,
    @Nullable FeedstockSeason season,
    @Nullable FeedstockPlanningCandidatePagePosition after,
    int limit
) {

  public FeedstockPlanningCandidateQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
    if (limit < 1 || limit > QueryLimits.MAXIMUM_PAGE_FETCH) {
      throw new ApplicationValidationException(
          "feedstock planning candidate limit must be between 1 and " + QueryLimits.MAXIMUM_PAGE_FETCH
      );
    }
  }
}
