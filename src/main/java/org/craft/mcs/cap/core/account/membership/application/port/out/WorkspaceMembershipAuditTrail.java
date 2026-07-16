package org.craft.mcs.cap.core.account.membership.application.port.out;

import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.jspecify.annotations.NonNull;


/** Membership-owned outbound boundary for recording administrative history. */
public interface WorkspaceMembershipAuditTrail {

  void recordRoleChanged(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership previous,
      @NonNull WorkspaceMembership updated
  );

  void recordRemoved(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership removed
  );

  void recordLeft(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership membership
  );

  void recordOwnershipTransferred(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership previousOwner,
      @NonNull WorkspaceMembership newOwner
  );

  void recordSuspended(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership previous,
      @NonNull WorkspaceMembership suspended
  );

  void recordReactivated(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership previous,
      @NonNull WorkspaceMembership reactivated
  );
}
