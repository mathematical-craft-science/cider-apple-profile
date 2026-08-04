package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;

import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/** Declares how source intervals participate in deterministic composition calculations. */
public enum CompositionIntervalPolicy {

  /** Uses exact interval midpoints, preserving the behavior of the previous axis pipeline. */
  MIDPOINT;

  public <T extends MeasuredValue> @NonNull BigDecimal representativeValue(
      @NonNull CharacteristicValue<T> value
  ) {
    return midpoint(value);
  }
}
