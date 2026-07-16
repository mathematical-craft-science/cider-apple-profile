package org.craft.mcs.cap.core.account.invitation.application.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Application read model of a workspace invitation at the requested point in time. */
public record WorkspaceInvitationView(
    @NonNull WorkspaceInvitationId invitationId,
    @NonNull WorkspaceId workspaceId,
    @NonNull EmailAddress invitedEmail,
    @NonNull AccountRole role,
    @NonNull WorkspaceInvitationStatus status,
    @NonNull AccountUserId invitedByUserId,
    @Nullable AccountUserId acceptedByUserId,
    @NonNull Instant expiresAt,
    int resendCount
) {

  public WorkspaceInvitationView {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(invitedEmail, "invited email cannot be null");
    requireNonNull(role, "invited workspace role cannot be null");
    requireNonNull(status, "workspace invitation status cannot be null");
    requireNonNull(invitedByUserId, "inviting user id cannot be null");
    requireNonNull(expiresAt, "workspace invitation expiration cannot be null");
  }
}
