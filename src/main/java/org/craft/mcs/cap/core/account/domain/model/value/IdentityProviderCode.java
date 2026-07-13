package org.craft.mcs.cap.core.account.domain.model.value;

import java.util.Locale;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Provider-neutral identity-provider code persisted by CAP. */
@ValueObject
public record IdentityProviderCode(@NonNull String value) {

  public static final @NonNull IdentityProviderCode CLERK = new IdentityProviderCode("CLERK");

  public IdentityProviderCode {
    requireNonNull(value, "identity provider code cannot be null");
    value = value.strip().toUpperCase(Locale.ROOT);
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("identity provider code cannot be blank");
    }
  }

  public static @NonNull IdentityProviderCode of(@NonNull String value) {
    return new IdentityProviderCode(value);
  }
}
