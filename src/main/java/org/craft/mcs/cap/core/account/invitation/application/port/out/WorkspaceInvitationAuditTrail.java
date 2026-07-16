package org.craft.mcs.cap.core.account.invitation.application.port.out;

import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.jspecify.annotations.NonNull;


/** Invitation-owned outbound boundary for recording invitation lifecycle history. */
public interface WorkspaceInvitationAuditTrail {

  void recordCreated(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitation invitation
  );

  void recordAccepted(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitation invitation
  );

  void recordRejected(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitation invitation
  );

  void recordRevoked(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitation invitation
  );

  void recordResent(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceInvitationStatus previousStatus,
      @NonNull WorkspaceInvitation invitation
  );
}
