package org.craft.mcs.cap.shared.kernel.identity;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


/** Identifies one immutable historical state of a physical material subject. */
@ValueObject
public record MaterialStateId(@NonNull UUID value) {

  public MaterialStateId {
    requireNonNull(value, "material state id cannot be null");
  }

  public static @NonNull MaterialStateId newId() {
    return new MaterialStateId(newUuid());
  }

  public static @NonNull MaterialStateId of(@NonNull UUID value) {
    return new MaterialStateId(value);
  }
}
