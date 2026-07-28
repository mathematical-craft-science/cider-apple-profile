package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper;

import java.math.BigDecimal;
import java.util.function.Function;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.CalculatedProfileCharacteristicValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.IntervalProfileCharacteristicValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.PointProfileCharacteristicValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueResponse;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps canonical characteristic values to their polymorphic REST representation. */
public final class ProfileCharacteristicRestMapper {

  private static final BigDecimal ONE_HALF = new BigDecimal("0.5");

  private ProfileCharacteristicRestMapper() {
  }

  public static @NonNull ProfileCharacteristicValueResponse toResponse(
      @NonNull AxisValue value,
      @NonNull String unit
  ) {
    requireNonNull(value, "profile axis value cannot be null");
    requireNonNull(unit, "profile axis value unit cannot be null");

    return switch (value.valueKind()) {
      case POINT -> new PointProfileCharacteristicValueResponse(value.value(), unit);
      case INTERVAL_MIDPOINT -> new IntervalProfileCharacteristicValueResponse(
          value.value(),
          requireNonNull(value.min(), "interval axis value minimum cannot be null"),
          requireNonNull(value.max(), "interval axis value maximum cannot be null"),
          unit
      );
      case CALCULATED -> new CalculatedProfileCharacteristicValueResponse(value.value(), unit);
    };
  }

  public static <T extends Comparable<? super T>> @NonNull ProfileCharacteristicValueResponse toResponse(
      @NonNull CharacteristicValue<T> value,
      @NonNull Function<T, BigDecimal> decimalValue,
      @NonNull String unit
  ) {
    requireNonNull(value, "profile characteristic cannot be null");
    requireNonNull(decimalValue, "profile characteristic decimal mapper cannot be null");
    requireNonNull(unit, "profile characteristic unit cannot be null");
    return switch (value) {
      case PointCharacteristicValue<T>(var point) ->
          new PointProfileCharacteristicValueResponse(decimalValue.apply(point), unit);
      case IntervalCharacteristicValue<T>(var lower, var upper) -> {
        final var lowerValue = decimalValue.apply(lower);
        final var upperValue = decimalValue.apply(upper);
        yield new IntervalProfileCharacteristicValueResponse(
            lowerValue.add(upperValue).multiply(ONE_HALF),
            lowerValue,
            upperValue,
            unit
        );
      }
    };
  }
}
