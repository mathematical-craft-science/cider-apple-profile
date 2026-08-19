package org.craft.mcs.cap.core.production.batch.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Immutable reference to the source evidence used to assess and later confirm production material.
 *
 * <p>The reference belongs to the production source contract rather than to a candidate. A saved
 * candidate retains it, and production confirmation compares it with current source evidence.</p>
 */
@ValueObject
public record ProductionSourceEvidenceReference(@NonNull String value) {

  private static final int MAX_LENGTH = 160;

  public ProductionSourceEvidenceReference {
    value = requireNonNull(value, "production source evidence reference cannot be null").strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException(
          "production source evidence reference cannot be blank"
      );
    }
    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "production source evidence reference length must not exceed " + MAX_LENGTH
      );
    }
  }

  public static @NonNull ProductionSourceEvidenceReference of(@NonNull String value) {
    return new ProductionSourceEvidenceReference(value);
  }
}
