package org.craft.mcs.cap.core.account.invitation.application.contract;

import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Identifies one generation of a workspace invitation that should be delivered asynchronously. */
public record WorkspaceInvitationDeliveryRequest(
    @NonNull WorkspaceInvitationId invitationId,
    int resendCount
) {

  public WorkspaceInvitationDeliveryRequest {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
  }

  /** Captures the identity and generation of the current invitation state. */
  public static @NonNull WorkspaceInvitationDeliveryRequest from(
      @NonNull WorkspaceInvitation invitation
  ) {
    requireNonNull(invitation, "workspace invitation cannot be null");
    return new WorkspaceInvitationDeliveryRequest(
        invitation.invitationId(),
        invitation.resendCount()
    );
  }
}
