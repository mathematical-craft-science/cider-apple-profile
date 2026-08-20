package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Maps typed characteristic values to the reusable Apple catalog JDBC column representation. */
public final class CharacteristicValueJdbcMapperSupport {

  private CharacteristicValueJdbcMapperSupport() {
  }

  public static <T extends Comparable<? super T>> @Nullable CharacteristicValueJdbcColumns
      fromNullableCharacteristic(
          @Nullable CharacteristicValue<T> characteristic,
          @NonNull Function<@NonNull T, @NonNull BigDecimal> valueExtractor
      ) {
    requireNonNull(valueExtractor, "value extractor cannot be null");
    return characteristic == null ? null : fromCharacteristic(characteristic, valueExtractor);
  }

  public static <T extends Comparable<? super T>> @NonNull CharacteristicValueJdbcColumns fromCharacteristic(
      @NonNull CharacteristicValue<T> characteristic,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> valueExtractor
  ) {
    requireNonNull(characteristic, "characteristic value cannot be null");
    requireNonNull(valueExtractor, "value extractor cannot be null");

    final var columns = new CharacteristicValueJdbcColumns();
    return switch (characteristic) {
      case PointCharacteristicValue<T>(var value) -> {
        columns.kind = CharacteristicValueStorageKind.POINT;
        columns.exactValue = valueExtractor.apply(value);
        yield columns;
      }
      case IntervalCharacteristicValue<T>(var min, var max) -> {
        columns.kind = CharacteristicValueStorageKind.INTERVAL;
        columns.minValue = valueExtractor.apply(min);
        columns.maxValue = valueExtractor.apply(max);
        yield columns;
      }
    };
  }

  public static <T extends Comparable<? super T>> @NonNull Optional<@NonNull CharacteristicValue<T>>
      toCharacteristic(
          @Nullable CharacteristicValueJdbcColumns source,
          @NonNull Function<@NonNull BigDecimal, @NonNull T> valueFactory
      ) {
    requireNonNull(valueFactory, "value factory cannot be null");
    if (source == null || source.kind == null) {
      return Optional.empty();
    }
    return switch (source.kind) {
      case POINT -> Optional.of(PointCharacteristicValue.of(
          valueFactory.apply(requireNonNull(source.exactValue, "point characteristic value cannot be null"))
      ));
      case INTERVAL -> Optional.of(IntervalCharacteristicValue.of(
          valueFactory.apply(requireNonNull(source.minValue, "interval lower value cannot be null")),
          valueFactory.apply(requireNonNull(source.maxValue, "interval upper value cannot be null"))
      ));
    };
  }
}
