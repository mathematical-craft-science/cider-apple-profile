package org.craft.mcs.cap.core.measurement.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Opaque external reference that identifies the evidence source without assuming a URI-only format.
 */
@ValueObject
public record MeasurementSourceReference(@NonNull String value) {

  private static final int MAX_LENGTH = 1_000;

  public MeasurementSourceReference {
    requireNonNull(value, "measurement source reference cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("measurement source reference cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "measurement source reference length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull MeasurementSourceReference of(@NonNull String value) {
    return new MeasurementSourceReference(value);
  }
}
