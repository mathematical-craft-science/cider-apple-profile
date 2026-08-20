package org.craft.mcs.cap.core.account.workspace.application.port.in;

import org.craft.mcs.cap.core.account.workspace.application.contract.WorkspaceSettingsView;
import org.jspecify.annotations.NonNull;

/** Updates workspace settings under explicit revision and permission checks. */
public interface UpdateWorkspaceSettingsUseCase {

  @NonNull WorkspaceSettingsView update(@NonNull UpdateWorkspaceSettingsCommand command);
}
