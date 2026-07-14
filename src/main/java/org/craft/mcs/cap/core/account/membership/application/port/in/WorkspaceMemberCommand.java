package org.craft.mcs.cap.core.account.membership.application.port.in;

import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Identifies one membership administration target inside a workspace. */
public record WorkspaceMemberCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull WorkspaceMembershipId membershipId
) {

  public WorkspaceMemberCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(membershipId, "workspace membership id cannot be null");
  }
}
