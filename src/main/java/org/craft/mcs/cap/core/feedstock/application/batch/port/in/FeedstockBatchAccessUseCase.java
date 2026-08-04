package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Public Feedstock boundary for authorizing access to one batch and resolving its workspace. */
public interface FeedstockBatchAccessUseCase {

  @NonNull WorkspaceId requireReadAccess(@NonNull FeedstockBatchId batchId);

  @NonNull WorkspaceId requireWriteAccess(@NonNull FeedstockBatchId batchId);

  void requireWorkspaceReadAccess(@NonNull WorkspaceId workspaceId);
}
