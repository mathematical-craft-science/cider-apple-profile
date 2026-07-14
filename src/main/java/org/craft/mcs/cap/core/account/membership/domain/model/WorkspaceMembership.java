package org.craft.mcs.cap.core.account.membership.domain.model;

import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
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

  /** Creates an active non-owner membership after an invitation has been accepted. */
  public static @NonNull WorkspaceMembership create(
      @NonNull WorkspaceId workspaceId,
      @NonNull AccountUserId userId,
      @NonNull AccountRole role
  ) {
    if (role == AccountRole.OWNER) {
      throw new WorkspaceMembershipRuleException("OWNER role can be assigned only by ownership transfer");
    }
    return new WorkspaceMembership(
        WorkspaceMembershipId.newId(),
        workspaceId,
        userId,
        role,
        WorkspaceMembershipStatus.ACTIVE
    );
  }

  public boolean isActive() {
    return status == WorkspaceMembershipStatus.ACTIVE;
  }

  public boolean isActiveOwner() {
    return isActive() && role == AccountRole.OWNER;
  }

  /** Changes a regular workspace role; OWNER assignment is reserved for ownership transfer. */
  public @NonNull WorkspaceMembership changeAssignableRole(@NonNull AccountRole updatedRole) {
    requireNonNull(updatedRole, "updated workspace role cannot be null");
    if (updatedRole == AccountRole.OWNER) {
      throw new WorkspaceMembershipRuleException("OWNER role can be assigned only by ownership transfer");
    }
    return with(updatedRole, status);
  }

  /** Temporarily disables this membership after workspace-level ownership checks have passed. */
  public @NonNull WorkspaceMembership suspend() {
    if (!isActive()) {
      throw new WorkspaceMembershipRuleException("only an active workspace membership can be suspended");
    }
    return with(role, WorkspaceMembershipStatus.SUSPENDED);
  }

  /** Restores a suspended membership with its existing role. */
  public @NonNull WorkspaceMembership reactivate() {
    if (isActive()) {
      throw new WorkspaceMembershipRuleException("only a suspended workspace membership can be reactivated");
    }
    return with(role, WorkspaceMembershipStatus.ACTIVE);
  }

  /** Promotes an active membership as the target of an explicit ownership transfer. */
  public @NonNull WorkspaceMembership promoteToOwner() {
    if (!isActive()) {
      throw new WorkspaceMembershipRuleException("ownership can be transferred only to an active membership");
    }
    return with(AccountRole.OWNER, status);
  }

  /** Demotes the transferring owner while keeping their workspace membership active. */
  public @NonNull WorkspaceMembership demoteOwnerToAdmin() {
    if (!isActiveOwner()) {
      throw new WorkspaceMembershipRuleException("ownership can be transferred only by an active owner");
    }
    return with(AccountRole.ADMIN, status);
  }

  private @NonNull WorkspaceMembership with(
      @NonNull AccountRole updatedRole,
      @NonNull WorkspaceMembershipStatus updatedStatus
  ) {
    return new WorkspaceMembership(membershipId, workspaceId, userId, updatedRole, updatedStatus);
  }
}
