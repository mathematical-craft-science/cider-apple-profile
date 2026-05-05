package org.craft.mcs.cap.core.appleprofile.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

@ValueObject
public record AppleVarietyId(@NonNull UUID value) {

  public AppleVarietyId {
    requireNonNull(value, "apple variety id cannot be null");
  }

  @NonNull
  public static AppleVarietyId newId() {
    return new AppleVarietyId(UUID.randomUUID());
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
