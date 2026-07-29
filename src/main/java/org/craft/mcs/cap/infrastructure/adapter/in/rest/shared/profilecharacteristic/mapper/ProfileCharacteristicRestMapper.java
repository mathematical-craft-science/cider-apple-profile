package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper;

import java.math.BigDecimal;
import java.util.function.Function;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.IntervalProfileCharacteristicValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.PointProfileCharacteristicValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueKind;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps canonical characteristic values to their polymorphic REST representation. */
public final class ProfileCharacteristicRestMapper {

  private static final BigDecimal ONE_HALF = new BigDecimal("0.5");

  private ProfileCharacteristicRestMapper() {
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

  /** Returns the transport value kind represented by a typed characteristic value. */
  public static <T extends Comparable<? super T>> @NonNull ProfileCharacteristicValueKind valueKind(
      @NonNull CharacteristicValue<T> value
  ) {
    requireNonNull(value, "profile characteristic cannot be null");
    return switch (value) {
      case PointCharacteristicValue<T> _ -> ProfileCharacteristicValueKind.POINT;
      case IntervalCharacteristicValue<T> _ -> ProfileCharacteristicValueKind.INTERVAL_MIDPOINT;
    };
  }
}
