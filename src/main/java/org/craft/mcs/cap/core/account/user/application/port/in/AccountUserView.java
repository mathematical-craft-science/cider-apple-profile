package org.craft.mcs.cap.core.account.user.application.port.in;

import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Read model of the CAP-owned user profile. */
public record AccountUserView(
    @NonNull AccountUserId userId,
    @NonNull EmailAddress email,
    @NonNull DisplayName displayName,
    @Nullable HttpUrl avatarUrl,
    @NonNull AccountUserStatus status
) {

  public AccountUserView {
    requireNonNull(userId, "account user id cannot be null");
    requireNonNull(email, "account email cannot be null");
    requireNonNull(displayName, "account display name cannot be null");
    requireNonNull(status, "account user status cannot be null");
  }

  /** Builds the application read model from the CAP-owned account aggregate. */
  public static @NonNull AccountUserView from(@NonNull AccountUser user) {
    requireNonNull(user, "account user cannot be null");
    return new AccountUserView(
        user.userId(),
        user.email(),
        user.displayName(),
        user.avatarUrl(),
        user.status()
    );
  }
}
