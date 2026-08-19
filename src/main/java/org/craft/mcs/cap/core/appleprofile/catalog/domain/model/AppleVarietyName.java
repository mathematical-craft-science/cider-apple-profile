package org.craft.mcs.cap.core.appleprofile.catalog.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

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
      throw new DomainRuleViolationException("apple variety name cannot be blank");
    }

    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "apple variety name length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  @NonNull
  public static AppleVarietyName of(@NonNull String value) {
    return new AppleVarietyName(value);
  }
}
