package org.craft.mcs.cap.shared.kernel.identity;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


/** Stable identifier of the CAP workspace that owns business data. */
@ValueObject
public record WorkspaceId(@NonNull UUID value) {

  public WorkspaceId {
    requireNonNull(value, "workspace id cannot be null");
  }

  public static @NonNull WorkspaceId newId() {
    return new WorkspaceId(newUuid());
  }

  public static @NonNull WorkspaceId of(@NonNull UUID value) {
    return new WorkspaceId(value);
  }

  public static @NonNull WorkspaceId of(@NonNull String value) {
    requireNonNull(value, "workspace id value cannot be null");
    return of(UUID.fromString(value));
  }
}
