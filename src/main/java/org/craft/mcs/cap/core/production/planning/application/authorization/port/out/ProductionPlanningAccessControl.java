package org.craft.mcs.cap.core.production.planning.application.authorization.port.out;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Authorizes production-planning reads and confirmed planning actions. */
public interface ProductionPlanningAccessControl {

  void requireReadAccess(@NonNull WorkspaceId workspaceId);

  void requireWriteAccess(@NonNull WorkspaceId workspaceId);
}
