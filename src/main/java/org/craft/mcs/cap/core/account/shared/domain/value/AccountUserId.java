package org.craft.mcs.cap.core.account.shared.domain.value;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;

/** Stable CAP identifier of an account user, independent of an external identity provider. */
@ValueObject
public record AccountUserId(@NonNull UUID value) {

  public AccountUserId {
    requireNonNull(value, "account user id cannot be null");
  }

  public static @NonNull AccountUserId newId() {
    return new AccountUserId(newUuid());
  }

  public static @NonNull AccountUserId of(@NonNull UUID value) {
    return new AccountUserId(value);
  }
}
