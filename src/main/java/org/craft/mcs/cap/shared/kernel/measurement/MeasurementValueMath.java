package org.craft.mcs.cap.shared.kernel.measurement;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Provides shape-only arithmetic shared by typed canonical measurement values.
 *
 * <p>A point is its own midpoint. An interval midpoint is an analytical representative and does
 * not become persisted measurement evidence.</p>
 */
public final class MeasurementValueMath {

  private static final BigDecimal ONE_HALF = new BigDecimal("0.5");

  private MeasurementValueMath() {
  }

  public static <T extends MeasuredValue> @NonNull BigDecimal midpoint(
      @NonNull CharacteristicValue<T> value
  ) {
    return switch (requireNonNull(value, "measurement value cannot be null")) {
      case PointCharacteristicValue<T>(var point) -> point.asBigDecimal();
      case IntervalCharacteristicValue<T>(var lower, var upper) ->
          midpoint(lower.asBigDecimal(), upper.asBigDecimal());
    };
  }

  public static @NonNull BigDecimal midpoint(
      @NonNull BigDecimal lower,
      @NonNull BigDecimal upper
  ) {
    requireNonNull(lower, "measurement interval lower value cannot be null");
    requireNonNull(upper, "measurement interval upper value cannot be null");
    return lower.add(upper).multiply(ONE_HALF);
  }
}
