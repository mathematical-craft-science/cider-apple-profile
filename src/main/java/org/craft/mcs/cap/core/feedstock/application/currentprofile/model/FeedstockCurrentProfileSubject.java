package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Identifies a Feedstock batch and its ownership scope for current-profile projection. */
public record FeedstockCurrentProfileSubject(
    @NonNull FeedstockBatchId batchId,
    @NonNull WorkspaceId workspaceId
) {

  public FeedstockCurrentProfileSubject {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(workspaceId, "feedstock workspace id cannot be null");
  }
}
