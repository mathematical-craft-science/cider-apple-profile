package org.craft.mcs.cap.core.account.domain.model;

import org.craft.mcs.cap.core.account.domain.model.value.AccountUserId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceMembershipId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** CAP-owned assignment of a user role inside one workspace. */
@AggregateRoot
public record WorkspaceMembership(
    @NonNull WorkspaceMembershipId membershipId,
    @NonNull WorkspaceId workspaceId,
    @NonNull AccountUserId userId,
    @NonNull AccountRole role,
    @NonNull WorkspaceMembershipStatus status
) {

  public WorkspaceMembership {
    requireNonNull(membershipId, "workspace membership id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(userId, "account user id cannot be null");
    requireNonNull(role, "workspace role cannot be null");
    requireNonNull(status, "workspace membership status cannot be null");
  }

  public static @NonNull WorkspaceMembership createOwner(
      @NonNull WorkspaceId workspaceId,
      @NonNull AccountUserId userId
  ) {
    return new WorkspaceMembership(
        WorkspaceMembershipId.newId(),
        workspaceId,
        userId,
        AccountRole.OWNER,
        WorkspaceMembershipStatus.ACTIVE
    );
  }

  public boolean isActive() {
    return status == WorkspaceMembershipStatus.ACTIVE;
  }
}
