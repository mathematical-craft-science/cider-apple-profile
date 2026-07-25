package org.craft.mcs.cap.core.production.application.port.out;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Resolves current immutable material states for Production batch read scenarios. */
public interface ProductionMaterialStateProvider {

  @NonNull MaterialStateId currentStateOf(@NonNull ProductionBatchId batchId);

  @NonNull Map<@NonNull ProductionBatchId, @NonNull MaterialStateId> currentStatesOf(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull ProductionBatchId> batchIds
  );
}
