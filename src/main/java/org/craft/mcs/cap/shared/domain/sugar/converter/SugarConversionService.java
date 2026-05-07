package org.craft.mcs.cap.shared.domain.sugar.converter;

import org.craft.mcs.cap.shared.domain.sugar.SugarValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class SugarConversionService {

  private final SugarConversionRegistry registry;

  public SugarConversionService(@NonNull SugarConversionRegistry registry) {
    this.registry = requireNonNull(registry);
  }

  @NonNull
  public <F extends SugarValue, T extends SugarValue> T convert(
      @NonNull F source,
      @NonNull Class<F> sourceType,
      @NonNull Class<T> targetType
  ) {
    requireNonNull(source);
    requireNonNull(sourceType);
    requireNonNull(targetType);

    var strategy = registry.find(sourceType, targetType)
        .orElseThrow(() -> new UnsupportedSugarConversionException(sourceType, targetType));

    return strategy.convert(source);
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public <F extends SugarValue, T extends SugarValue> T convert(
      @NonNull F source,
      @NonNull Class<T> targetType
  ) {
    requireNonNull(source);
    requireNonNull(targetType);

    return convert(source, (Class<F>) source.getClass(), targetType);
  }
}
