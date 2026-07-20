package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.model.FeedstockMeasurementSugarAcidityPagePosition;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchCurrentSugarAcidityZonePointQuery(
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockMeasurementPointFilters filters,
    @NonNull CursorPageRequest<@NonNull FeedstockMeasurementSugarAcidityPagePosition> page
) {

  public FeedstockBatchCurrentSugarAcidityZonePointQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock batch current point filters cannot be null");
    requireNonNull(page, "cursor page request cannot be null");
  }
}
