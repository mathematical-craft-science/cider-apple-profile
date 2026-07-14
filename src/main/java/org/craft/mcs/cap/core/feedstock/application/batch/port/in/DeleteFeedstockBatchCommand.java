package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record DeleteFeedstockBatchCommand(
    @NonNull FeedstockBatchId batchId,
    @NonNull WorkspaceId workspaceId
) {

  public DeleteFeedstockBatchCommand {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
  }
}
