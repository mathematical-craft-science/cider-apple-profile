package org.craft.mcs.cap.shared.domain.measurement.value;

import java.math.BigDecimal;
import java.util.Optional;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

@ValueObject
public record StatisticalMeasurement<T extends Comparable<T>>(
    @NonNull T mean,
    @NonNull Optional<@NonNull T> median,
    @NonNull Optional<@NonNull BigDecimal> standardDeviation,
    @NonNull Optional<@NonNull Integer> sampleSize
) implements MeasuredCharacteristic<T> {

  public StatisticalMeasurement {
    requireNonNull(mean, "mean measured value cannot be null");
    requireNonNull(median, "median measured value cannot be null");
    requireNonNull(standardDeviation, "standard deviation cannot be null");
    requireNonNull(sampleSize, "sample size cannot be null");

    standardDeviation.ifPresent(value -> {
      if (value.signum() < 0) {
        throw new IllegalArgumentException("standard deviation must not be negative");
      }
    });

    sampleSize.ifPresent(value -> {
      if (value <= 0) {
        throw new IllegalArgumentException("sample size must be positive");
      }
    });
  }

  @NonNull
  public static <T extends Comparable<T>> StatisticalMeasurement<T> mean(@NonNull T mean) {
    return new StatisticalMeasurement<>(
        mean,
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );
  }

  @Override
  @NonNull
  public Optional<@NonNull T> representativeValue() {
    return Optional.of(mean);
  }
}
