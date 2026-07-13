package org.craft.mcs.cap.core.account.domain.model.value;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Stable identifier of a user's membership in a CAP workspace. */
@ValueObject
public record WorkspaceMembershipId(@NonNull UUID value) {

  public WorkspaceMembershipId {
    requireNonNull(value, "workspace membership id cannot be null");
  }

  public static @NonNull WorkspaceMembershipId newId() {
    return new WorkspaceMembershipId(UUID.randomUUID());
  }

  public static @NonNull WorkspaceMembershipId of(@NonNull UUID value) {
    return new WorkspaceMembershipId(value);
  }
}
