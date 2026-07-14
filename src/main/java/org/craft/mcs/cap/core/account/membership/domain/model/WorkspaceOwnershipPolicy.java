package org.craft.mcs.cap.core.account.membership.domain.model;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Enforces the cross-membership invariant that every active workspace retains an active owner. */
public final class WorkspaceOwnershipPolicy {

  /** Verifies that a membership can be suspended, removed, or voluntarily left. */
  public void requireCanDeactivate(
      @NonNull WorkspaceMembership membership,
      @NonNull List<@NonNull WorkspaceMembership> workspaceMemberships
  ) {
    requireNonNull(membership, "workspace membership cannot be null");
    requireNonNull(workspaceMemberships, "workspace memberships cannot be null");
    if (membership.isActiveOwner() && activeOwnerCount(workspaceMemberships) <= 1) {
      throw new WorkspaceMembershipRuleException("the last active workspace owner cannot be deactivated");
    }
  }

  /** Produces both membership changes required for one atomic ownership transfer. */
  public @NonNull OwnershipTransfer transfer(
      @NonNull WorkspaceMembership currentOwner,
      @NonNull WorkspaceMembership targetMembership
  ) {
    requireNonNull(currentOwner, "current owner membership cannot be null");
    requireNonNull(targetMembership, "target membership cannot be null");
    if (!currentOwner.workspaceId().equals(targetMembership.workspaceId())) {
      throw new WorkspaceMembershipRuleException("ownership transfer memberships must belong to one workspace");
    }
    if (currentOwner.membershipId().equals(targetMembership.membershipId())) {
      throw new WorkspaceMembershipRuleException("ownership transfer target must be another membership");
    }
    return new OwnershipTransfer(
        currentOwner.demoteOwnerToAdmin(),
        targetMembership.promoteToOwner()
    );
  }

  private static long activeOwnerCount(@NonNull List<@NonNull WorkspaceMembership> memberships) {
    return memberships.stream().filter(WorkspaceMembership::isActiveOwner).count();
  }

  /** Membership states that must be persisted together inside the ownership-transfer transaction. */
  public record OwnershipTransfer(
      @NonNull WorkspaceMembership previousOwner,
      @NonNull WorkspaceMembership newOwner
  ) {

    public OwnershipTransfer {
      requireNonNull(previousOwner, "previous owner membership cannot be null");
      requireNonNull(newOwner, "new owner membership cannot be null");
    }
  }
}
