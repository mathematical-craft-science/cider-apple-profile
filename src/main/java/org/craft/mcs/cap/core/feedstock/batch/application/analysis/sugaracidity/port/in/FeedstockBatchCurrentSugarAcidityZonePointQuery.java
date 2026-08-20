package org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileFilters;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePagePosition;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphCriteria;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchCurrentSugarAcidityZonePointQuery(
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockCurrentProfileFilters filters,
    @Nullable FeedstockSugarAcidityGraphCriteria graphCriteria,
    @NonNull CursorPageRequest<@NonNull FeedstockCurrentProfilePagePosition> page
) {

  public FeedstockBatchCurrentSugarAcidityZonePointQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock batch current point filters cannot be null");
    requireNonNull(page, "cursor page request cannot be null");
  }
}
