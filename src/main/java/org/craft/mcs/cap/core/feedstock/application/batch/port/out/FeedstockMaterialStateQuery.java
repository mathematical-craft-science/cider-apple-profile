package org.craft.mcs.cap.core.feedstock.application.batch.port.out;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Read port for current material-state identities stored by Feedstock aggregates. */
public interface FeedstockMaterialStateQuery {

  @NonNull Map<@NonNull FeedstockBatchId, @NonNull MaterialStateId> findCurrentStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  );
}
