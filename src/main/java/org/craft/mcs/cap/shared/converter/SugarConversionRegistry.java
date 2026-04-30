package org.craft.mcs.cap.shared.converter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.craft.mcs.cap.shared.SugarValue;

public final class SugarConversionRegistry {

  private final Map<ConversionKey, SugarConversionStrategy<?, ?>> strategies;

  public SugarConversionRegistry(List<SugarConversionStrategy<?, ?>> strategies) {
    this.strategies = strategies.stream()
        .collect(Collectors.toUnmodifiableMap(
            strategy -> ConversionKey.of(strategy.sourceType(), strategy.targetType()),
            Function.identity()
        ));
  }

  @SuppressWarnings("unchecked")
  public <F extends SugarValue, T extends SugarValue> Optional<SugarConversionStrategy<F, T>> find(
      Class<F> sourceType,
      Class<T> targetType
  ) {
    return Optional.ofNullable(strategies.get(ConversionKey.of(sourceType, targetType)))
        .map(strategy -> (SugarConversionStrategy<F, T>) strategy);
  }
}
