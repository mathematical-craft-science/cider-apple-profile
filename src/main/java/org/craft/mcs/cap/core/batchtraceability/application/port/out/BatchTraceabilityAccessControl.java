package org.craft.mcs.cap.core.batchtraceability.application.port.out;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Authorizes read access to traceability facts owned by a workspace. */
@FunctionalInterface
public interface BatchTraceabilityAccessControl {

  void requireReadAccess(@NonNull WorkspaceId workspaceId);
}
