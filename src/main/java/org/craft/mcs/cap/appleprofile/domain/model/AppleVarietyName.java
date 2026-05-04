package org.craft.mcs.cap.appleprofile.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

@ValueObject
public record AppleVarietyName(@NonNull String value) {

  private static final int MAX_LENGTH = 120;

  public AppleVarietyName {
    requireNonNull(value, "apple variety name cannot be null");
    value = value.strip();

    if (value.isBlank()) {
      throw new IllegalArgumentException("apple variety name cannot be blank");
    }

    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "apple variety name length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  @NonNull
  public static AppleVarietyName of(@NonNull String value) {
    return new AppleVarietyName(value);
  }
}
