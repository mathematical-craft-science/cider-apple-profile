package org.craft.mcs.cap.core.production.application.port.in;

import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Public Production boundary for authorizing access to one batch and resolving its workspace. */
public interface ProductionBatchAccessUseCase {

  @NonNull WorkspaceId requireReadAccess(@NonNull ProductionBatchId batchId);

  @NonNull WorkspaceId requireWriteAccess(@NonNull ProductionBatchId batchId);

  void requireWorkspaceReadAccess(@NonNull WorkspaceId workspaceId);
}
