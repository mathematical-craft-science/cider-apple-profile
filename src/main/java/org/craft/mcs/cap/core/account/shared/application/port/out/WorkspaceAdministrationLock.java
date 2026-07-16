package org.craft.mcs.cap.core.account.shared.application.port.out;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Serializes workspace administration commands whose invariants span multiple aggregates. */
public interface WorkspaceAdministrationLock {

  void acquire(@NonNull WorkspaceId workspaceId);
}
