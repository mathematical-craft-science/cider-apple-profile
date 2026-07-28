package org.craft.mcs.cap.shared.kernel.sugar.converter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class SugarConversionRegistry {

  private final Map<ConversionKey, SugarConversionStrategy<?, ?>> strategies;

  public SugarConversionRegistry(@NonNull List<SugarConversionStrategy<?, ?>> strategies) {
    this.strategies = requireNonNull(strategies).stream()
        .collect(Collectors.toUnmodifiableMap(
            strategy -> ConversionKey.of(strategy.sourceType(), strategy.targetType()),
            Function.identity()
        ));
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public <F extends MeasuredValue, T extends MeasuredValue> Optional<SugarConversionStrategy<F, T>> find(
      @NonNull Class<F> sourceType,
      @NonNull Class<T> targetType
  ) {
    return Optional.ofNullable(strategies.get(ConversionKey.of(sourceType, targetType)))
        .map(strategy -> (SugarConversionStrategy<F, T>) strategy);
  }
}
