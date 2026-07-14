package org.craft.mcs.cap.core.account.workspace.application.port.in;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Reads settings for a workspace accessible to the current account. */
public interface GetWorkspaceSettingsUseCase {

  @NonNull WorkspaceSettingsView get(@NonNull WorkspaceId workspaceId);
}
