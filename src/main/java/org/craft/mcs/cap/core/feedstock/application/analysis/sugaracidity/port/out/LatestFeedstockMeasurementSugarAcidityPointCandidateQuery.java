package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record LatestFeedstockMeasurementSugarAcidityPointCandidateQuery(
    @NonNull WorkspaceId workspaceId,
    @Nullable FeedstockBatchCriteria batch,
    @Nullable FeedstockSeason season,
    @Nullable FeedstockMeasurementSugarAcidityPointCandidateCursor after,
    int limit
) {

  public LatestFeedstockMeasurementSugarAcidityPointCandidateQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");

    if (limit < 1 || limit > CursorPageRequest.MAX_LIMIT) {
      throw new ApplicationValidationException(
          "latest candidate query limit must be between 1 and " + CursorPageRequest.MAX_LIMIT
      );
    }
  }
}
