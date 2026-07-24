package org.craft.mcs.cap.core.measurement.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Human-readable name of the instrument, laboratory, institute, curator, or imported source. */
@ValueObject
public record MeasurementSourceName(@NonNull String value) {

  private static final int MAX_LENGTH = 255;

  public MeasurementSourceName {
    requireNonNull(value, "measurement source name cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("measurement source name cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "measurement source name length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull MeasurementSourceName of(@NonNull String value) {
    return new MeasurementSourceName(value);
  }
}
