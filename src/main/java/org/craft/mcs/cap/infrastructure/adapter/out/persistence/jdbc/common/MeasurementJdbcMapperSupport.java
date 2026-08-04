package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class MeasurementJdbcMapperSupport {

  private MeasurementJdbcMapperSupport() {
  }

  public static <T extends Comparable<? super T>> @Nullable MeasurementJdbcColumns fromNullableCharacteristic(
      @Nullable CharacteristicValue<T> characteristic,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> valueExtractor
  ) {
    requireNonNull(valueExtractor, "value extractor cannot be null");

    if (characteristic == null) {
      return null;
    }

    return fromCharacteristic(characteristic, valueExtractor);
  }

  public static <T extends Comparable<? super T>> @NonNull MeasurementJdbcColumns fromCharacteristic(
      @NonNull CharacteristicValue<T> characteristic,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> valueExtractor
  ) {
    requireNonNull(characteristic, "measured characteristic cannot be null");
    requireNonNull(valueExtractor, "value extractor cannot be null");

    final var embeddable = new MeasurementJdbcColumns();

    return switch (characteristic) {
      case PointCharacteristicValue<T>(T value) -> {
        embeddable.kind = MeasurementKind.POINT;
        embeddable.exactValue = valueExtractor.apply(value);
        yield embeddable;
      }
      case IntervalCharacteristicValue<T>(T min, T max) -> {
        embeddable.kind = MeasurementKind.INTERVAL;
        embeddable.minValue = valueExtractor.apply(min);
        embeddable.maxValue = valueExtractor.apply(max);
        yield embeddable;
      }
    };
  }

  public static <T extends Comparable<? super T>> @NonNull Optional<@NonNull CharacteristicValue<T>> toCharacteristic(
      @Nullable MeasurementJdbcColumns source,
      @NonNull Function<@NonNull BigDecimal, @NonNull T> valueFactory
  ) {
    requireNonNull(valueFactory, "value factory cannot be null");

    if (source == null || source.kind == null) {
      return Optional.empty();
    }

    return switch (source.kind) {
      case POINT -> Optional.of(PointCharacteristicValue.of(
          valueFactory.apply(requireNonNull(source.exactValue, "point measurement value cannot be null"))
      ));
      case INTERVAL -> Optional.of(IntervalCharacteristicValue.of(
          valueFactory.apply(requireNonNull(source.minValue, "interval lower value cannot be null")),
          valueFactory.apply(requireNonNull(source.maxValue, "interval upper value cannot be null"))
      ));
    };
  }
}
