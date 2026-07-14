package org.craft.mcs.cap.core.account.user.application.port.in;

import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Command for updating CAP-owned profile fields. */
public record UpdateAccountProfileCommand(
    @NonNull DisplayName displayName,
    @Nullable HttpUrl avatarUrl
) {

  public UpdateAccountProfileCommand {
    requireNonNull(displayName, "account display name cannot be null");
  }
}
