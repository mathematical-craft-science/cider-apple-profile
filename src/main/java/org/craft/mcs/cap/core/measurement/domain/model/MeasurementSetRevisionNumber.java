package org.craft.mcs.cap.core.measurement.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;


/** Positive sequence number of an immutable revision within one measurement set. */
@ValueObject
public record MeasurementSetRevisionNumber(int value) {

  private static final int INITIAL_VALUE = 1;

  public MeasurementSetRevisionNumber {
    if (value < INITIAL_VALUE) {
      throw new DomainRuleViolationException("measurement revision number must be positive");
    }
  }

  @NonNull
  public static MeasurementSetRevisionNumber initial() {
    return new MeasurementSetRevisionNumber(INITIAL_VALUE);
  }

  @NonNull
  public static MeasurementSetRevisionNumber of(int value) {
    return new MeasurementSetRevisionNumber(value);
  }

  public boolean isInitial() {
    return value == INITIAL_VALUE;
  }

  @NonNull
  public MeasurementSetRevisionNumber next() {
    if (value == Integer.MAX_VALUE) {
      throw new DomainRuleViolationException("measurement revision number cannot be incremented");
    }
    return new MeasurementSetRevisionNumber(value + 1);
  }
}
