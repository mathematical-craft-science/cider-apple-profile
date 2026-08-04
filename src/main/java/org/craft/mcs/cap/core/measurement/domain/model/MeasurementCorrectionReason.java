package org.craft.mcs.cap.core.measurement.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Required explanation of why an immutable measurement set was corrected. */
@ValueObject
public record MeasurementCorrectionReason(@NonNull String value) {

  private static final int MAX_LENGTH = 1_000;

  public MeasurementCorrectionReason {
    requireNonNull(value, "measurement correction reason cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new DomainRuleViolationException("measurement correction reason cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "measurement correction reason length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull MeasurementCorrectionReason of(@NonNull String value) {
    return new MeasurementCorrectionReason(value);
  }
}
