package org.craft.mcs.cap.core.feedstock.application.detail.port.in;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Query for a feedstock batch detail read model scoped by owner.
 */
public record GetFeedstockBatchDetailQuery(
    @NonNull FeedstockBatchId batchId,
    @NonNull WorkspaceId workspaceId
) {

  public GetFeedstockBatchDetailQuery {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
  }
}
