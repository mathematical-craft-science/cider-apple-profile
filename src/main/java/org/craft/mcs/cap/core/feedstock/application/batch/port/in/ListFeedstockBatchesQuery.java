package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record ListFeedstockBatchesQuery(
    @NonNull WorkspaceId workspaceId,
    @Nullable FeedstockSeason season
) {

  public ListFeedstockBatchesQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
  }
}
