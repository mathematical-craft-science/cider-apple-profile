package org.craft.mcs.cap.shared.testing;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.TemperatureValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.TurbidityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Test fixture for constructing concrete typed values in characteristic-parameterized tests. */
public final class MeasurementTestValues {

  private MeasurementTestValues() {
  }

  public static @NonNull MeasuredValue value(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull BigDecimal value
  ) {
    requireNonNull(characteristic, "measurement characteristic cannot be null");
    requireNonNull(value, "measurement value cannot be null");
    return switch (characteristic) {
      case SPECIFIC_GRAVITY -> SpecificGravityValue.of(value);
      case PH -> PhValue.of(value);
      case TITRATABLE_ACIDITY -> TitratableAcidityValue.of(value);
      case TANNIN -> TanninValue.of(value);
      case YAN -> YeastAssimilableNitrogenValue.of(value);
      case TEMPERATURE -> TemperatureValue.of(value);
      case TURBIDITY -> TurbidityValue.of(value);
    };
  }

  public static @NonNull PointCharacteristicValue<MeasuredValue> point(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull BigDecimal value
  ) {
    return PointCharacteristicValue.of(value(characteristic, value));
  }

  public static @NonNull IntervalCharacteristicValue<MeasuredValue> interval(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull BigDecimal lower,
      @NonNull BigDecimal upper
  ) {
    return IntervalCharacteristicValue.of(
        value(characteristic, lower),
        value(characteristic, upper)
    );
  }

  public static @NonNull MeasuredValue pointValue(
      @NonNull CharacteristicValue<MeasuredValue> value
  ) {
    return switch (value) {
      case PointCharacteristicValue<MeasuredValue>(var point) -> point;
      default -> throw new AssertionError("Expected point characteristic value");
    };
  }
}
