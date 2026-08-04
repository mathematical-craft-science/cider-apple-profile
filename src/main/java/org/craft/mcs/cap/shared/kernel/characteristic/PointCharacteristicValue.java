package org.craft.mcs.cap.shared.kernel.characteristic;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Represents one retained point of a typed characteristic, whether measured or calculated. */
@ValueObject
public record PointCharacteristicValue<T extends Comparable<? super T>>(
    @NonNull T value
) implements CharacteristicValue<T> {

  public PointCharacteristicValue {
    requireNonNull(value, "characteristic point value cannot be null");
  }

  public static <T extends Comparable<? super T>> @NonNull PointCharacteristicValue<T> of(
      @NonNull T value
  ) {
    return new PointCharacteristicValue<>(value);
  }
}
