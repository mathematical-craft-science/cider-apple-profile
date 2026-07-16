package org.craft.mcs.cap.core.account.administration.application.port.in;

import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Changes a member's non-owner role inside one workspace. */
public record ChangeWorkspaceMemberRoleCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull WorkspaceMembershipId membershipId,
    @NonNull AccountRole role
) {

  public ChangeWorkspaceMemberRoleCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(membershipId, "workspace membership id cannot be null");
    requireNonNull(role, "workspace role cannot be null");
  }
}
