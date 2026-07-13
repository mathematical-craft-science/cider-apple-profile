package org.craft.mcs.cap.core.account.application.port.in;

import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.HttpUrl;
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
