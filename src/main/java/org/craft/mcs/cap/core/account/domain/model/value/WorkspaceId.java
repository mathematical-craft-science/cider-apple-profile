package org.craft.mcs.cap.core.account.domain.model.value;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Stable CAP identifier of a product workspace. */
@ValueObject
public record WorkspaceId(@NonNull UUID value) {

  public WorkspaceId {
    requireNonNull(value, "workspace id cannot be null");
  }

  public static @NonNull WorkspaceId newId() {
    return new WorkspaceId(UUID.randomUUID());
  }

  public static @NonNull WorkspaceId of(@NonNull UUID value) {
    return new WorkspaceId(value);
  }
}
