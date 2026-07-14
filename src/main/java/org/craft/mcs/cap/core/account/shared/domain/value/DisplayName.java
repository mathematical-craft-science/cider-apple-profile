package org.craft.mcs.cap.core.account.shared.domain.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Human-readable name displayed for an account-domain person or organization. */
@ValueObject
public record DisplayName(@NonNull String value) {

  private static final int MAX_LENGTH = 255;

  public DisplayName {
    requireNonNull(value, "display name cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("display name cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException("display name cannot exceed 255 characters");
    }
  }

  public static @NonNull DisplayName of(@NonNull String value) {
    return new DisplayName(value);
  }
}
