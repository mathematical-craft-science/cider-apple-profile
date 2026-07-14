package org.craft.mcs.cap.core.account.membership.application.port.in;

import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Transfers ownership from the current active owner to another active membership. */
public record TransferWorkspaceOwnershipCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull WorkspaceMembershipId targetMembershipId
) {

  public TransferWorkspaceOwnershipCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(targetMembershipId, "target workspace membership id cannot be null");
  }
}
