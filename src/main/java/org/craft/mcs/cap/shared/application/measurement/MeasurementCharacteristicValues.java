package org.craft.mcs.cap.shared.application.measurement;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Safely narrows heterogeneous canonical measurement values to a scenario-owned value type. */
public final class MeasurementCharacteristicValues {

  private MeasurementCharacteristicValues() {
  }

  public static <T extends MeasuredValue> @NonNull CharacteristicValue<T> requireType(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull CharacteristicValue<MeasuredValue> value,
      @NonNull Class<T> valueType
  ) {
    requireNonNull(characteristic, "measurement characteristic cannot be null");
    requireNonNull(value, "measurement characteristic value cannot be null");
    requireNonNull(valueType, "measurement value type cannot be null");
    return switch (value) {
      case PointCharacteristicValue<MeasuredValue>(var point) ->
          PointCharacteristicValue.of(requireType(characteristic, point, valueType));
      case IntervalCharacteristicValue<MeasuredValue>(var lower, var upper) ->
          IntervalCharacteristicValue.of(
              requireType(characteristic, lower, valueType),
              requireType(characteristic, upper, valueType)
          );
    };
  }

  private static <T extends MeasuredValue> @NonNull T requireType(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull MeasuredValue value,
      @NonNull Class<T> valueType
  ) {
    if (!valueType.isInstance(value)) {
      throw new ApplicationValidationException(
          "measurement characteristic " + characteristic + " has incompatible value type "
              + value.getClass()
      );
    }
    return valueType.cast(value);
  }
}
