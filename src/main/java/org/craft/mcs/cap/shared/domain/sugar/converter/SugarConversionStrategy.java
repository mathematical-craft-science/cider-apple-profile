package org.craft.mcs.cap.shared.domain.sugar.converter;

import org.craft.mcs.cap.shared.domain.sugar.SugarValue;

public interface SugarConversionStrategy<F extends SugarValue, T extends SugarValue> {

  Class<F> sourceType();

  Class<T> targetType();

  T convert(F source);

  default boolean supports(Class<?> source, Class<?> target) {
    return sourceType().equals(source) && targetType().equals(target);
  }
}