package org.craft.mcs.cap.shared.domain.measurement.converter;

import org.craft.mcs.cap.shared.domain.measurement.value.SugarValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ConversionKey(
    @NonNull Class<? extends SugarValue> source,
    @NonNull Class<? extends SugarValue> target
) {

  public ConversionKey {
    requireNonNull(source);
    requireNonNull(target);
  }

  @NonNull
  public static ConversionKey of(
      @NonNull Class<? extends SugarValue> source,
      @NonNull Class<? extends SugarValue> target
  ) {
    return new ConversionKey(source, target);
  }
}
