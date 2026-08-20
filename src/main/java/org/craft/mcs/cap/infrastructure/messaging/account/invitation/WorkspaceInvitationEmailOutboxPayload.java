package org.craft.mcs.cap.infrastructure.messaging.account.invitation;

import java.util.UUID;
import org.craft.mcs.cap.core.account.invitation.application.contract.WorkspaceInvitationDeliveryRequest;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Serializable outbox payload identifying one invitation delivery generation. */
public record WorkspaceInvitationEmailOutboxPayload(
    @NonNull UUID invitationId,
    int resendCount
) {

  public WorkspaceInvitationEmailOutboxPayload {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
  }

  public static @NonNull WorkspaceInvitationEmailOutboxPayload from(
      @NonNull WorkspaceInvitationDeliveryRequest request
  ) {
    requireNonNull(request, "workspace invitation delivery request cannot be null");
    return new WorkspaceInvitationEmailOutboxPayload(
        request.invitationId().value(),
        request.resendCount()
    );
  }

  public @NonNull WorkspaceInvitationDeliveryRequest toRequest() {
    return new WorkspaceInvitationDeliveryRequest(
        WorkspaceInvitationId.of(invitationId),
        resendCount
    );
  }
}
