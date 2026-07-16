package org.craft.mcs.cap.infrastructure.adapter.out.audit.account;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.port.in.WorkspaceAdministrativeAuditRecorder;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationAuditTrail;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.INVITATION_ACCEPTED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.INVITATION_CREATED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.INVITATION_REJECTED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.INVITATION_RESENT;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.INVITATION_REVOKED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType.INVITATION;
import static org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus.PENDING;


/** Maps invitation semantic audit operations to the account audit context. */
@Component
@RequiredArgsConstructor
public final class AccountWorkspaceInvitationAuditTrail implements WorkspaceInvitationAuditTrail {

  private final @NonNull WorkspaceAdministrativeAuditRecorder auditRecorder;

  @Override
  public void recordCreated(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitation invitation
  ) {
    append(
        actorUserId,
        invitation,
        INVITATION_CREATED,
        null,
        WorkspaceAdministrativeAuditValue.of(invitation.role().name())
    );
  }

  @Override
  public void recordAccepted(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitation invitation
  ) {
    recordStatusChange(actorUserId, invitation, INVITATION_ACCEPTED, PENDING);
  }

  @Override
  public void recordRejected(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitation invitation
  ) {
    recordStatusChange(actorUserId, invitation, INVITATION_REJECTED, PENDING);
  }

  @Override
  public void recordRevoked(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitation invitation
  ) {
    recordStatusChange(actorUserId, invitation, INVITATION_REVOKED, PENDING);
  }

  @Override
  public void recordResent(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitationStatus previousStatus,
      @NonNull WorkspaceInvitation invitation
  ) {
    recordStatusChange(actorUserId, invitation, INVITATION_RESENT, previousStatus);
  }

  private void recordStatusChange(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitation invitation,
      @NonNull WorkspaceAdministrativeAction action,
      @NonNull WorkspaceInvitationStatus previousStatus
  ) {
    append(
        actorUserId,
        invitation,
        action,
        WorkspaceAdministrativeAuditValue.of(previousStatus.name()),
        WorkspaceAdministrativeAuditValue.of(invitation.status().name())
    );
  }

  private void append(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitation invitation,
      @NonNull WorkspaceAdministrativeAction action,
      WorkspaceAdministrativeAuditValue previous,
      WorkspaceAdministrativeAuditValue updated
  ) {
    auditRecorder.append(
        invitation.workspaceId(),
        actorUserId,
        action,
        INVITATION,
        WorkspaceAdministrativeSubjectId.of(invitation.invitationId().value()),
        previous,
        updated
    );
  }
}
