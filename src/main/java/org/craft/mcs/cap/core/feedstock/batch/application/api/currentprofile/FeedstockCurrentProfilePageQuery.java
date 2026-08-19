package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;

import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Query for a stable page of state-aware current Feedstock profiles. */
public record FeedstockCurrentProfilePageQuery(
    @NonNull WorkspaceId workspaceId,
    @Nullable FeedstockBatchCriteria batch,
    @Nullable FeedstockSeason season,
    @Nullable FeedstockCurrentProfilePagePosition after,
    int limit
) {

  public FeedstockCurrentProfilePageQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");

    if (limit < 1 || limit > QueryLimits.MAXIMUM_PAGE_FETCH) {
      throw new ApplicationValidationException(
          "current candidate query limit must be between 1 and " + QueryLimits.MAXIMUM_PAGE_FETCH
      );
    }
  }

  /** Returns the same selection criteria positioned at another keyset page. */
  public @NonNull FeedstockCurrentProfilePageQuery at(
      @Nullable FeedstockCurrentProfilePagePosition nextPosition,
      int nextLimit
  ) {
    return new FeedstockCurrentProfilePageQuery(
        workspaceId,
        batch,
        season,
        nextPosition,
        nextLimit
    );
  }
}
