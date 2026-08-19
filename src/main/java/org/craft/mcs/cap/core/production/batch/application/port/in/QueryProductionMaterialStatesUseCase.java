package org.craft.mcs.cap.core.production.batch.application.port.in;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Publishes current material-state identities owned by Production batches. */
public interface QueryProductionMaterialStatesUseCase {

  @NonNull Map<@NonNull ProductionBatchId, @NonNull MaterialStateId> findCurrentStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull ProductionBatchId> batchIds
  );
}
