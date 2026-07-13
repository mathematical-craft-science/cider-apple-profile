package org.craft.mcs.cap.core.account.application.port.in;

import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceMembershipId;
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
}
