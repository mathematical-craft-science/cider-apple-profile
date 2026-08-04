package org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out;

import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Query for current Feedstock batch evidence that may participate in an analysis scenario. */
public record CurrentFeedstockBatchAnalysisCandidateQuery(
    @NonNull WorkspaceId workspaceId,
    @Nullable FeedstockBatchCriteria batch,
    @Nullable FeedstockSeason season,
    @Nullable FeedstockAnalysisCandidatePagePosition after,
    int limit
) {

  public CurrentFeedstockBatchAnalysisCandidateQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");

    if (limit < 1 || limit > QueryLimits.MAXIMUM_PAGE_FETCH) {
      throw new ApplicationValidationException(
          "current candidate query limit must be between 1 and " + QueryLimits.MAXIMUM_PAGE_FETCH
      );
    }
  }
}
