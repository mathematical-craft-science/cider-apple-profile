package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class MeasurementJdbcMapperSupport {

  private MeasurementJdbcMapperSupport() {
  }

  public static <T extends Comparable<T>> @Nullable MeasurementJdbcColumns fromNullableCharacteristic(
      @Nullable MeasuredCharacteristic<T> characteristic,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> valueExtractor
  ) {
    requireNonNull(valueExtractor, "value extractor cannot be null");

    if (characteristic == null) {
      return null;
    }

    return fromCharacteristic(characteristic, valueExtractor);
  }

  public static <T extends Comparable<T>> @NonNull MeasurementJdbcColumns fromCharacteristic(
      @NonNull MeasuredCharacteristic<T> characteristic,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> valueExtractor
  ) {
    requireNonNull(characteristic, "measured characteristic cannot be null");
    requireNonNull(valueExtractor, "value extractor cannot be null");

    final var embeddable = new MeasurementJdbcColumns();

    switch (characteristic) {
      case ExactMeasurement<T>(T value) -> {
        embeddable.kind = MeasurementKind.EXACT;
        embeddable.exactValue = valueExtractor.apply(value);
        return embeddable;
      }
      case RangeMeasurement<T>(T min, T max) -> {
        embeddable.kind = MeasurementKind.RANGE;
        embeddable.minValue = valueExtractor.apply(min);
        embeddable.maxValue = valueExtractor.apply(max);
        return embeddable;
      }
      case StatisticalMeasurement<T>(
          T mean,
          T median,
          BigDecimal standardDeviation,
          Integer sampleSize
      ) -> {
        embeddable.kind = MeasurementKind.STATISTICAL;
        embeddable.meanValue = valueExtractor.apply(mean);
        embeddable.medianValue = median == null ? null : valueExtractor.apply(median);
        embeddable.standardDeviation = standardDeviation;
        embeddable.sampleSize = sampleSize;
        return embeddable;
      }
      default -> {
      }
    }

    throw new IllegalArgumentException(
        "unsupported measured characteristic type: " + characteristic.getClass().getName()
    );
  }

  public static <T extends Comparable<T>> @NonNull Optional<@NonNull MeasuredCharacteristic<T>> toCharacteristic(
      @Nullable MeasurementJdbcColumns source,
      @NonNull Function<@NonNull BigDecimal, @NonNull T> valueFactory
  ) {
    requireNonNull(valueFactory, "value factory cannot be null");

    if (source == null || source.kind == null) {
      return Optional.empty();
    }

    return switch (source.kind) {
      case EXACT -> Optional.of(ExactMeasurement.of(
          valueFactory.apply(requireNonNull(source.exactValue, "exact measurement value cannot be null"))
      ));
      case RANGE -> Optional.of(RangeMeasurement.of(
          valueFactory.apply(requireNonNull(source.minValue, "range minimum value cannot be null")),
          valueFactory.apply(requireNonNull(source.maxValue, "range maximum value cannot be null"))
      ));
      case STATISTICAL -> Optional.of(StatisticalMeasurement.of(
          valueFactory.apply(requireNonNull(source.meanValue, "statistical mean value cannot be null")),
          source.medianValue == null ? null : valueFactory.apply(source.medianValue),
          source.standardDeviation,
          source.sampleSize
      ));
    };
  }
}