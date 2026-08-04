package org.craft.mcs.cap.core.measurement.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Required explanation of why a complete logical measurement set was retracted. */
@ValueObject
public record MeasurementRetractionReason(@NonNull String value) {

  private static final int MAX_LENGTH = 1_000;

  public MeasurementRetractionReason {
    requireNonNull(value, "measurement retraction reason cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("measurement retraction reason cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "measurement retraction reason length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull MeasurementRetractionReason of(@NonNull String value) {
    return new MeasurementRetractionReason(value);
  }
}
