package org.craft.mcs.cap.shared.kernel.identity;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Stable provider-neutral reference to a CAP-owned actor recorded in audit history. */
@ValueObject
public record ActorReference(
    @NonNull ActorType type,
    @NonNull UUID id
) {

  public ActorReference {
    requireNonNull(type, "actor type cannot be null");
    requireNonNull(id, "actor id cannot be null");
  }

  /** Creates a reference to a human CAP account. */
  public static @NonNull ActorReference accountUser(@NonNull UUID accountUserId) {
    return new ActorReference(ActorType.ACCOUNT_USER, accountUserId);
  }

  /** Creates a reference to a CAP machine identity. */
  public static @NonNull ActorReference machineIdentity(@NonNull UUID machineIdentityId) {
    return new ActorReference(ActorType.MACHINE_IDENTITY, machineIdentityId);
  }
}
