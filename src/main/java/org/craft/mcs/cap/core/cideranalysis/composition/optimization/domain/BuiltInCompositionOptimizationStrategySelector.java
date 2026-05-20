package org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain;

import java.util.EnumMap;
import java.util.Map;
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
public final class BuiltInCompositionOptimizationStrategySelector implements CompositionOptimizationStrategySelector {

  private final @NonNull Map<@NonNull CompositionOptimizationAlgorithm, @NonNull ChemicalProfileCompositionOptimizationStrategy> strategies;

  public BuiltInCompositionOptimizationStrategySelector() {
    final var builtInStrategies = new EnumMap<CompositionOptimizationAlgorithm, ChemicalProfileCompositionOptimizationStrategy>(
        CompositionOptimizationAlgorithm.class
    );
    builtInStrategies.put(
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH,
        new DiscretizedBoundedChemicalProfileCompositionOptimizationStrategy()
    );
    this.strategies = Map.copyOf(builtInStrategies);
  }

  @Override
  public @NonNull ChemicalProfileCompositionOptimizationStrategy select(
      @NonNull CompositionOptimizationAlgorithm algorithm
  ) {
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    final var strategy = strategies.get(algorithm);
    if (strategy == null) { // NOSONAR
      throw new UnsupportedCompositionOptimizationStrategyException(algorithm);
    }
    return strategy;
  }
}
