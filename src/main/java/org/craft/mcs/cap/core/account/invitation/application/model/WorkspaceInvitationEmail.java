package org.craft.mcs.cap.core.account.invitation.application.model;

import java.time.Instant;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Provider-neutral content required to send one workspace invitation email. */
public record WorkspaceInvitationEmail(
    @NonNull WorkspaceInvitationId invitationId,
    @NonNull EmailAddress recipient,
    @NonNull DisplayName workspaceName,
    @NonNull AccountRole role,
    @NonNull DisplayName inviterName,
    @NonNull Instant expiresAt
) {

  public WorkspaceInvitationEmail {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
    requireNonNull(recipient, "workspace invitation recipient cannot be null");
    requireNonNull(workspaceName, "workspace name cannot be null");
    requireNonNull(role, "workspace invitation role cannot be null");
    requireNonNull(inviterName, "workspace invitation inviter name cannot be null");
    requireNonNull(expiresAt, "workspace invitation expiration cannot be null");
  }
}
