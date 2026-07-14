package org.craft.mcs.cap.core.account.invitation.application.port.in;

import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Identifies one administrator-managed invitation inside an active workspace context. */
public record WorkspaceInvitationCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull WorkspaceInvitationId invitationId
) {

  public WorkspaceInvitationCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(invitationId, "workspace invitation id cannot be null");
  }
}
