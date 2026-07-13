package org.craft.mcs.cap.core.account.domain.model.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Phone number preserving international formatting without imposing a country-specific numbering plan. */
@ValueObject
public record PhoneNumber(@NonNull String value) {

  private static final int MAX_LENGTH = 64;

  public PhoneNumber {
    requireNonNull(value, "phone number cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("phone number cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException("phone number cannot exceed 64 characters");
    }
    if (value.chars().noneMatch(Character::isDigit)) {
      throw new DomainRuleViolationException("phone number must contain at least one digit");
    }
    if (value.chars().anyMatch(Character::isISOControl)) {
      throw new DomainRuleViolationException("phone number cannot contain control characters");
    }
  }

  public static @NonNull PhoneNumber of(@NonNull String value) {
    return new PhoneNumber(value);
  }

  public static @Nullable PhoneNumber fromNullable(@Nullable String value) {
    return value == null || value.isBlank() ? null : of(value);
  }
}
