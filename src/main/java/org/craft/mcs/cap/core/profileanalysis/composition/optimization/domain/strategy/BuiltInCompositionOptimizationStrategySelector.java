package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import static java.util.Objects.requireNonNull;


/**
 * Selects one of the built-in composition optimization algorithms.
 *
 * <p>Only {@link CompositionOptimizationAlgorithm#DISCRETIZED_BOUNDED_SEARCH} is executable today.
 * The enum already contains future algorithm codes such as linear, quadratic, mixed-integer, local
 * search and evolutionary approaches, but this selector deliberately fails fast if a caller asks for
 * an algorithm that has not been implemented and registered.
 */
public final class BuiltInCompositionOptimizationStrategySelector
    implements CompositionOptimizationStrategySelector {

  private final @NonNull Map<
      @NonNull CompositionOptimizationAlgorithm,
      @NonNull CompositionOptimizationStrategy
      > strategies;

  public BuiltInCompositionOptimizationStrategySelector() {
    final var builtInStrategies = new EnumMap<
        CompositionOptimizationAlgorithm,
        CompositionOptimizationStrategy
        >(
        CompositionOptimizationAlgorithm.class
    );
    builtInStrategies.put(
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH,
        new DiscretizedBoundedCompositionOptimizationStrategy()
    );
    this.strategies = Map.copyOf(builtInStrategies);
  }

  @Override
  public @NonNull CompositionOptimizationStrategy select(
      @NonNull CompositionOptimizationAlgorithm algorithm
  ) {
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    return Optional.ofNullable(strategies.get(algorithm))
        .orElseThrow(() -> new UnsupportedCompositionOptimizationStrategyException(algorithm));
  }
}
