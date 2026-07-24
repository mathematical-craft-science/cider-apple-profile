package org.craft.mcs.cap.core.measurement.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Named procedure or analytical method used to obtain a measurement result. */
@ValueObject
public record MeasurementMethod(@NonNull String value) {

  private static final int MAX_LENGTH = 255;

  public MeasurementMethod {
    requireNonNull(value, "measurement method cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("measurement method cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "measurement method length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull MeasurementMethod of(@NonNull String value) {
    return new MeasurementMethod(value);
  }
}
