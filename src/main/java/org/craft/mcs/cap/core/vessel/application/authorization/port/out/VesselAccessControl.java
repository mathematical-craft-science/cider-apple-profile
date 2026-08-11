package org.craft.mcs.cap.core.vessel.application.authorization.port.out;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Outbound authorization boundary for workspace-owned vessel catalog data. */
public interface VesselAccessControl {

  void requireReadAccess(@NonNull WorkspaceId workspaceId);

  void requireWriteAccess(@NonNull WorkspaceId workspaceId);
}
