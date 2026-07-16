package org.craft.mcs.cap.core.account.invitation.application.port.in;

import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Minimal CAP-owned profile of the account that sent a workspace invitation. */
public record WorkspaceInvitationInviterView(
    @NonNull AccountUserId userId,
    @NonNull DisplayName displayName,
    @Nullable HttpUrl avatarUrl
) {

  public WorkspaceInvitationInviterView {
    requireNonNull(userId, "inviting account user id cannot be null");
    requireNonNull(displayName, "inviting account display name cannot be null");
  }

  /** Builds a privacy-limited inviter view from the CAP account aggregate. */
  public static @NonNull WorkspaceInvitationInviterView from(@NonNull AccountUser user) {
    requireNonNull(user, "inviting account cannot be null");
    return new WorkspaceInvitationInviterView(
        user.userId(),
        user.displayName(),
        user.avatarUrl()
    );
  }
}
