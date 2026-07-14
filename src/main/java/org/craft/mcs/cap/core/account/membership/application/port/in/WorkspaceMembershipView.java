package org.craft.mcs.cap.core.account.membership.application.port.in;

import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Read model joining membership state with its workspace. */
public record WorkspaceMembershipView(
    @NonNull WorkspaceMembershipId membershipId,
    @NonNull WorkspaceView workspace,
    @NonNull AccountRole role,
    @NonNull WorkspaceMembershipStatus status
) {

  public WorkspaceMembershipView {
    requireNonNull(membershipId, "workspace membership id cannot be null");
    requireNonNull(workspace, "workspace cannot be null");
    requireNonNull(role, "workspace role cannot be null");
    requireNonNull(status, "workspace membership status cannot be null");
  }

  /** Builds a membership read model together with its workspace projection. */
  public static @NonNull WorkspaceMembershipView from(
      @NonNull WorkspaceMembership membership,
      @NonNull Workspace workspace
  ) {
    requireNonNull(membership, "workspace membership cannot be null");
    requireNonNull(workspace, "workspace cannot be null");
    return new WorkspaceMembershipView(
        membership.membershipId(),
        WorkspaceView.from(workspace),
        membership.role(),
        membership.status()
    );
  }
}
