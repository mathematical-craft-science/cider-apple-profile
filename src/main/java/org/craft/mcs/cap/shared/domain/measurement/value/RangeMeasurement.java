package org.craft.mcs.cap.shared.domain.measurement.value;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record RangeMeasurement<T extends Comparable<T>>(
    @NonNull T min,
    @NonNull T max
) implements MeasuredCharacteristic<T> {

  public RangeMeasurement {
    requireNonNull(min, "minimum measured value cannot be null");
    requireNonNull(max, "maximum measured value cannot be null");

    if (min.compareTo(max) > 0) {
      throw new IllegalArgumentException("minimum measured value must not exceed maximum");
    }
  }

  @NonNull
  public static <T extends Comparable<T>> RangeMeasurement<T> of(
      @NonNull T min,
      @NonNull T max
  ) {
    return new RangeMeasurement<>(min, max);
  }

}
