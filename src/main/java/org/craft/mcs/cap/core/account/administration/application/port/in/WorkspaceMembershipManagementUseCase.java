package org.craft.mcs.cap.core.account.administration.application.port.in;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Manages member roles, access state, removal, voluntary leave, and ownership transfer. */
public interface WorkspaceMembershipManagementUseCase {

  @NonNull WorkspaceMemberView changeRole(@NonNull ChangeWorkspaceMemberRoleCommand command);

  @NonNull WorkspaceMemberView suspend(@NonNull WorkspaceMemberCommand command);

  @NonNull WorkspaceMemberView reactivate(@NonNull WorkspaceMemberCommand command);

  void remove(@NonNull WorkspaceMemberCommand command);

  void leave(@NonNull WorkspaceId workspaceId);

  @NonNull WorkspaceMemberView transferOwnership(
      @NonNull TransferWorkspaceOwnershipCommand command
  );
}
