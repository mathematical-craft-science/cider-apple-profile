package org.craft.mcs.cap.shared.kernel.sugar.converter;

import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ConversionKey(
    @NonNull Class<? extends MeasuredValue> source,
    @NonNull Class<? extends MeasuredValue> target
) {

  public ConversionKey {
    requireNonNull(source);
    requireNonNull(target);
  }

  @NonNull
  public static ConversionKey of(
      @NonNull Class<? extends MeasuredValue> source,
      @NonNull Class<? extends MeasuredValue> target
  ) {
    return new ConversionKey(source, target);
  }
}
