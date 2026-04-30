package org.craft.mcs.cap.shared.converter;

import org.craft.mcs.cap.shared.SugarValue;

public record ConversionKey(
    Class<? extends SugarValue> source,
    Class<? extends SugarValue> target
) {

  public static ConversionKey of(
      Class<? extends SugarValue> source,
      Class<? extends SugarValue> target
  ) {
    return new ConversionKey(source, target);
  }
}
