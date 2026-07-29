package org.craft.mcs.cap.core.production.application.port.out;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Read port for current material-state identities stored by Production aggregates. */
public interface ProductionMaterialStateQuery {

  @NonNull Map<@NonNull ProductionBatchId, @NonNull MaterialStateId> findCurrentStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull ProductionBatchId> batchIds
  );
}
