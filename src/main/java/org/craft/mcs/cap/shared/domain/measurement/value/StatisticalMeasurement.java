package org.craft.mcs.cap.shared.domain.measurement.value;

import java.math.BigDecimal;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@ValueObject
public record StatisticalMeasurement<T extends Comparable<T>>(
    @NonNull T mean,
    @Nullable T median,
    @Nullable BigDecimal standardDeviation,
    @Nullable Integer sampleSize
) implements MeasuredCharacteristic<T> {

  public StatisticalMeasurement {
    requireNonNull(mean, "mean measured value cannot be null");

    if (standardDeviation != null && standardDeviation.signum() < 0) {
      throw new IllegalArgumentException("standard deviation must not be negative");
    }

    if (sampleSize != null && sampleSize <= 0) {
      throw new IllegalArgumentException("sample size must be positive");
    }
  }

  public static <T extends Comparable<T>> @NonNull StatisticalMeasurement<T> mean(
      @NonNull T mean
  ) {
    return new StatisticalMeasurement<>(mean, null, null, null);
  }

  public static <T extends Comparable<T>> @NonNull StatisticalMeasurement<T> of(
      @NonNull T mean,
      @Nullable T median,
      @Nullable BigDecimal standardDeviation,
      @Nullable Integer sampleSize
  ) {
    return new StatisticalMeasurement<>(mean, median, standardDeviation, sampleSize);
  }

}
