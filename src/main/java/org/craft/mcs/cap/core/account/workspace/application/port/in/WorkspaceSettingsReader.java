package org.craft.mcs.cap.core.account.workspace.application.port.in;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Read-only Account application API for persisted workspace product settings. */
public interface WorkspaceSettingsReader {

  @NonNull WorkspaceSettingsView readCurrent();

  @NonNull WorkspaceSettingsView readByWorkspaceId(@NonNull WorkspaceId workspaceId);
}
