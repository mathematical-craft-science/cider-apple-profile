package org.craft.mcs.cap.core.measurement.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Human-readable name of a logical measurement set. */
@ValueObject
public record MeasurementSetName(@NonNull String value) {

  private static final int MAX_LENGTH = 160;

  public MeasurementSetName {
    requireNonNull(value, "measurement set name cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("measurement set name cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "measurement set name length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull MeasurementSetName of(@NonNull String value) {
    return new MeasurementSetName(value);
  }
}
