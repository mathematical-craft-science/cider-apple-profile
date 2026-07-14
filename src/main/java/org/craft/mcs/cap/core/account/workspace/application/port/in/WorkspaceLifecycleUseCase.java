package org.craft.mcs.cap.core.account.workspace.application.port.in;

import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Manages creation and editable lifecycle of CAP-owned workspaces. */
public interface WorkspaceLifecycleUseCase {

  @NonNull WorkspaceMembershipView create(@NonNull CreateWorkspaceCommand command);

  @NonNull WorkspaceView update(@NonNull UpdateWorkspaceCommand command);

  @NonNull WorkspaceView archive(@NonNull WorkspaceId workspaceId);
}
