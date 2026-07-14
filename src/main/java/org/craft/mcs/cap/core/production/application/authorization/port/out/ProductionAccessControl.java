package org.craft.mcs.cap.core.production.application.authorization.port.out;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Outbound authorization boundary for production data owned by a workspace scope. */
public interface ProductionAccessControl {

  void requireReadAccess(@NonNull WorkspaceId workspaceId);

  void requireWriteAccess(@NonNull WorkspaceId workspaceId);
}
