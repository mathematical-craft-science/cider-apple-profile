package org.craft.mcs.cap.core.profileanalysis.classification.domain.engine;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;

import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/** Declares how a classification strategy obtains one value when its input is an interval. */
public enum ClassificationIntervalPolicy {

  /**
   * Matches rules by interval midpoint and lets confidence assessment report boundary overlap.
   */
  MIDPOINT_WITH_BOUNDARY_EVIDENCE;

  public <T extends MeasuredValue> @NonNull BigDecimal representativeValue(
      @NonNull CharacteristicValue<T> value
  ) {
    return midpoint(value);
  }
}
