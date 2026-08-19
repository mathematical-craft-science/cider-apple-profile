package org.craft.mcs.cap.core.appleprofile.catalog.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;

@ValueObject
public record AppleVarietyId(@NonNull UUID value) {

  public AppleVarietyId {
    requireNonNull(value, "apple variety id cannot be null");
  }

  @NonNull
  public static AppleVarietyId newId() {
    return new AppleVarietyId(newUuid());
  }

  @NonNull
  public static AppleVarietyId of(@NonNull UUID value) {
    return new AppleVarietyId(value);
  }

  @NonNull
  public static AppleVarietyId of(@NonNull String value) {
    requireNonNull(value, "apple variety id value cannot be null");
    return of(UUID.fromString(value));
  }
}
