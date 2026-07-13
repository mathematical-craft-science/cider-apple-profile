package org.craft.mcs.cap.core.account.application.port.in;

import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceMembershipId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Workspace member read model combining CAP user and membership data. */
public record WorkspaceMemberView(
    @NonNull WorkspaceMembershipId membershipId,
    @NonNull AccountUserView user,
    @NonNull AccountRole role,
    @NonNull WorkspaceMembershipStatus status
) {

  public WorkspaceMemberView {
    requireNonNull(membershipId, "workspace membership id cannot be null");
    requireNonNull(user, "account user cannot be null");
    requireNonNull(role, "workspace role cannot be null");
    requireNonNull(status, "workspace membership status cannot be null");
  }
}
