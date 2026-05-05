package org.craft.mcs.cap.shared.domain.measurement.value;

import java.util.Optional;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

@ValueObject
public record ExactMeasurement<T extends Comparable<T>>(@NonNull T value)
    implements MeasuredCharacteristic<T> {

  public ExactMeasurement {
    requireNonNull(value, "measured value cannot be null");
  }

  @NonNull
  public static <T extends Comparable<T>> ExactMeasurement<T> of(@NonNull T value) {
    return new ExactMeasurement<>(value);
  }

  @Override
  @NonNull
  public Optional<@NonNull T> representativeValue() {
    return Optional.of(value);
  }
}
