package org.craft.mcs.cap.core.account.domain.model.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Opaque user subject issued by the configured identity provider. */
@ValueObject
public record ExternalUserSubject(@NonNull String value) {

  public ExternalUserSubject {
    requireNonNull(value, "external user subject cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("external user subject cannot be blank");
    }
  }

  public static @NonNull ExternalUserSubject of(@NonNull String value) {
    return new ExternalUserSubject(value);
  }
}
