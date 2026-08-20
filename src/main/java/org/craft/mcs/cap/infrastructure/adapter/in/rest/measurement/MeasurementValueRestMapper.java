package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement;

import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.IntervalMeasurementValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementValueKind;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.PointMeasurementValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;


/** Maps canonical measurement values to explicit value-and-unit transport variants. */
public final class MeasurementValueRestMapper {

  private MeasurementValueRestMapper() {
  }

  public static @NonNull MeasurementValueResponse toResponse(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull CharacteristicValue<MeasuredValue> value
  ) {
    return switch (value) {
      case PointCharacteristicValue<MeasuredValue>(var measuredValue) ->
          new PointMeasurementValueResponse(
              MeasurementValueKind.POINT,
              quantity(characteristic, measuredValue)
          );
      case IntervalCharacteristicValue<MeasuredValue>(var lower, var upper) ->
          new IntervalMeasurementValueResponse(
              MeasurementValueKind.INTERVAL,
              quantity(characteristic, lower),
              quantity(characteristic, upper)
          );
    };
  }

  private static @NonNull DecimalQuantityResponse quantity(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull MeasuredValue value
  ) {
    return new DecimalQuantityResponse(
        value.asBigDecimal(),
        CanonicalUnitLabels.forCharacteristic(characteristic)
    );
  }
}
