package org.craft.mcs.cap.core.appleprofile.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Human-readable source citation for an AppleVariety catalog reference profile. */
@ValueObject
public record ReferenceProfileSource(@NonNull String value) {

  public ReferenceProfileSource {
    requireNonNull(value, "apple variety reference profile source cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("apple variety reference profile source cannot be blank");
    }
    if (value.length() > 255) {
      throw new DomainRuleViolationException("apple variety reference profile source exceeds 255 characters");
    }
  }

  public static @NonNull ReferenceProfileSource of(@NonNull String value) {
    return new ReferenceProfileSource(value);
  }
}
