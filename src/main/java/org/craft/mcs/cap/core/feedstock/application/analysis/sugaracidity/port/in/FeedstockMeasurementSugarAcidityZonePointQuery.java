package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockMeasurementSugarAcidityZonePointQuery(
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockAnalysisCandidateFilters filters,
    @NonNull CursorPageRequest<@NonNull FeedstockAnalysisCandidatePagePosition> page
) {

  public FeedstockMeasurementSugarAcidityZonePointQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    requireNonNull(page, "cursor page request cannot be null");
  }
}
