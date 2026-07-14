package org.craft.mcs.cap.core.account.shared.domain.value;

import java.util.Locale;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Provider-verified account email address stored in normalized textual form. */
@ValueObject
public record EmailAddress(@NonNull String value) {

  private static final int MAX_LENGTH = 320;

  public EmailAddress {
    requireNonNull(value, "email address cannot be null");
    value = value.strip().toLowerCase(Locale.ROOT);
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("email address cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException("email address cannot exceed 320 characters");
    }
    final var separator = value.indexOf('@');
    if (separator <= 0
        || separator != value.lastIndexOf('@')
        || separator == value.length() - 1
        || value.chars().anyMatch(Character::isWhitespace)) {
      throw new DomainRuleViolationException("email address has invalid format");
    }
  }

  public static @NonNull EmailAddress of(@NonNull String value) {
    return new EmailAddress(value);
  }

  public static @Nullable EmailAddress fromNullable(@Nullable String value) {
    return value == null || value.isBlank() ? null : of(value);
  }

  /** Returns the local part suitable as a fallback account display name. */
  public @NonNull String localPart() {
    return value.substring(0, value.indexOf('@'));
  }
}
