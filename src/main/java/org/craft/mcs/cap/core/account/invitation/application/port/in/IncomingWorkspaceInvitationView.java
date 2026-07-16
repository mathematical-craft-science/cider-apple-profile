package org.craft.mcs.cap.core.account.invitation.application.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Recipient-facing invitation preview with workspace and inviter context. */
public record IncomingWorkspaceInvitationView(
    @NonNull WorkspaceInvitationId invitationId,
    @NonNull WorkspaceView workspace,
    @NonNull AccountRole role,
    @NonNull WorkspaceInvitationStatus status,
    @NonNull WorkspaceInvitationInviterView invitedBy,
    @NonNull Instant expiresAt
) {

  public IncomingWorkspaceInvitationView {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
    requireNonNull(workspace, "invited workspace cannot be null");
    requireNonNull(role, "invited workspace role cannot be null");
    requireNonNull(status, "workspace invitation status cannot be null");
    requireNonNull(invitedBy, "inviting account cannot be null");
    requireNonNull(expiresAt, "workspace invitation expiration cannot be null");
  }
}
