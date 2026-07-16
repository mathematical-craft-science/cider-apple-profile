package org.craft.mcs.cap.core.account.invitation.application.port.in;

import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Invites an email address to join a workspace with a non-owner role. */
public record InviteWorkspaceMemberCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull EmailAddress invitedEmail,
    @NonNull AccountRole role
) {

  public InviteWorkspaceMemberCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(invitedEmail, "invited email cannot be null");
    requireNonNull(role, "invited workspace role cannot be null");
  }
}
