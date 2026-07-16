package org.craft.mcs.cap.infrastructure.adapter.out.audit.account;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.port.in.WorkspaceAdministrativeAuditRecorder;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipAuditTrail;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.MEMBERSHIP_LEFT;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.MEMBERSHIP_REACTIVATED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.MEMBERSHIP_REMOVED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.MEMBERSHIP_ROLE_CHANGED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.MEMBERSHIP_SUSPENDED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.OWNERSHIP_TRANSFERRED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType.MEMBERSHIP;


/** Maps membership semantic audit operations to the account audit context. */
@Component
@RequiredArgsConstructor
public final class AccountWorkspaceMembershipAuditTrail implements WorkspaceMembershipAuditTrail {

  private final @NonNull WorkspaceAdministrativeAuditRecorder auditRecorder;

  @Override
  public void recordRoleChanged(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership previous,
      @NonNull WorkspaceMembership updated
  ) {
    append(
        actorUserId,
        updated,
        MEMBERSHIP_ROLE_CHANGED,
        WorkspaceAdministrativeAuditValue.of(previous.role().name()),
        WorkspaceAdministrativeAuditValue.of(updated.role().name())
    );
  }

  @Override
  public void recordRemoved(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership removed
  ) {
    append(actorUserId, removed, MEMBERSHIP_REMOVED, snapshot(removed), null);
  }

  @Override
  public void recordLeft(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership membership
  ) {
    append(actorUserId, membership, MEMBERSHIP_LEFT, snapshot(membership), null);
  }

  @Override
  public void recordOwnershipTransferred(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership previousOwner,
      @NonNull WorkspaceMembership newOwner
  ) {
    append(
        actorUserId,
        newOwner,
        OWNERSHIP_TRANSFERRED,
        WorkspaceAdministrativeAuditValue.of(previousOwner.membershipId().value().toString()),
        WorkspaceAdministrativeAuditValue.of(newOwner.membershipId().value().toString())
    );
  }

  @Override
  public void recordSuspended(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership previous,
      @NonNull WorkspaceMembership suspended
  ) {
    recordStatusChange(actorUserId, previous, suspended, MEMBERSHIP_SUSPENDED);
  }

  @Override
  public void recordReactivated(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership previous,
      @NonNull WorkspaceMembership reactivated
  ) {
    recordStatusChange(actorUserId, previous, reactivated, MEMBERSHIP_REACTIVATED);
  }

  private void recordStatusChange(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership previous,
      @NonNull WorkspaceMembership updated,
      @NonNull WorkspaceAdministrativeAction action
  ) {
    append(
        actorUserId,
        updated,
        action,
        WorkspaceAdministrativeAuditValue.of(previous.status().name()),
        WorkspaceAdministrativeAuditValue.of(updated.status().name())
    );
  }

  private void append(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership membership,
      @NonNull WorkspaceAdministrativeAction action,
      WorkspaceAdministrativeAuditValue previous,
      WorkspaceAdministrativeAuditValue updated
  ) {
    auditRecorder.append(
        membership.workspaceId(),
        actorUserId,
        action,
        MEMBERSHIP,
        WorkspaceAdministrativeSubjectId.of(membership.membershipId().value()),
        previous,
        updated
    );
  }

  private static @NonNull WorkspaceAdministrativeAuditValue snapshot(
      @NonNull WorkspaceMembership membership
  ) {
    return WorkspaceAdministrativeAuditValue.of("userId=" + membership.userId().value()
        + ";role=" + membership.role()
        + ";status=" + membership.status());
  }
}
