package org.craft.mcs.cap.core.feedstock.application.inventory.port.out;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Resolves the current immutable material state owned by a feedstock batch. */
public interface FeedstockMaterialStateProvider {

  @NonNull MaterialStateId currentStateOf(@NonNull FeedstockBatchId batchId);

  @NonNull Map<@NonNull FeedstockBatchId, @NonNull MaterialStateId> currentStatesOf(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  );
}
