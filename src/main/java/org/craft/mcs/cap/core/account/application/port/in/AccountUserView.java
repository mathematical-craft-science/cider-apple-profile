package org.craft.mcs.cap.core.account.application.port.in;

import org.craft.mcs.cap.core.account.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.account.domain.model.value.AccountUserId;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.EmailAddress;
import org.craft.mcs.cap.core.account.domain.model.value.HttpUrl;
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
}
