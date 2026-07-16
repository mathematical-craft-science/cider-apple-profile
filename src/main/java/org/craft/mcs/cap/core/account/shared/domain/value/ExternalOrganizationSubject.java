package org.craft.mcs.cap.core.account.shared.domain.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Optional opaque organization subject supplied by an external identity provider. */
@ValueObject
public record ExternalOrganizationSubject(@NonNull String value) {

  public ExternalOrganizationSubject {
    requireNonNull(value, "external organization subject cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("external organization subject cannot be blank");
    }
  }

  public static @NonNull ExternalOrganizationSubject of(@NonNull String value) {
    return new ExternalOrganizationSubject(value);
  }
}
