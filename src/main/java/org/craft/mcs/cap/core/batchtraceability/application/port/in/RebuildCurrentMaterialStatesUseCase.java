package org.craft.mcs.cap.core.batchtraceability.application.port.in;

import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialStatesRebuildResult;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Reconstructs the disposable current-state projection from immutable trace facts. */
public interface RebuildCurrentMaterialStatesUseCase {

  @NonNull
  CurrentMaterialStatesRebuildResult rebuild(@NonNull WorkspaceId workspaceId);
}
