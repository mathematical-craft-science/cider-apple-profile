package org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Publishes current material-state identities owned by Feedstock batches. */
public interface QueryFeedstockMaterialStatesUseCase {

  @NonNull Map<@NonNull FeedstockBatchId, @NonNull MaterialStateId> findCurrentStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  );
}
