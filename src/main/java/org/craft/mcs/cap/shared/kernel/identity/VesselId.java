package org.craft.mcs.cap.shared.kernel.identity;

import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


/** Stable identity of a vessel referenced by material-owning bounded contexts. */
@ValueObject
public record VesselId(@NonNull UUID value) {

  public VesselId {
    requireNonNull(value, "vessel id cannot be null");
    if (value.version() != 7 || value.variant() != 2) {
      throw new DomainRuleViolationException(
          "vessel id must be an RFC 9562 UUID version 7"
      );
    }
  }

  public static @NonNull VesselId newId() {
    return new VesselId(newUuid());
  }

  public static @NonNull VesselId of(@NonNull UUID value) {
    return new VesselId(value);
  }
}
