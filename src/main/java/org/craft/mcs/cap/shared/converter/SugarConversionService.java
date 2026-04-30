package org.craft.mcs.cap.shared.converter;

import org.craft.mcs.cap.shared.SugarValue;

public final class SugarConversionService {

  private final SugarConversionRegistry registry;

  public SugarConversionService(SugarConversionRegistry registry) {
    this.registry = registry;
  }

  public <F extends SugarValue, T extends SugarValue> T convert(
      F source,
      Class<F> sourceType,
      Class<T> targetType
  ) {
    var strategy = registry.find(sourceType, targetType)
        .orElseThrow(() -> new UnsupportedSugarConversionException(sourceType, targetType));

    return strategy.convert(source);
  }

  @SuppressWarnings("unchecked")
  public <F extends SugarValue, T extends SugarValue> T convert(
      F source,
      Class<T> targetType
  ) {
    return convert(source, (Class<F>) source.getClass(), targetType);
  }
}
