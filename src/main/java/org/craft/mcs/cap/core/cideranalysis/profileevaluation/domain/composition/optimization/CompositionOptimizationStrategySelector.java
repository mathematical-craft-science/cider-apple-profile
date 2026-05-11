package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization;

import org.jspecify.annotations.NonNull;


/**
 * Selects the optimization algorithm implementation by stable algorithm code.
 *
 * <p>Application services depend on this selector instead of concrete algorithms. This keeps the
 * REST/application contract open for future solvers such as linear programming or nonlinear
 * optimization without changing the command orchestration.
 */
public interface CompositionOptimizationStrategySelector {

  @NonNull
  ChemicalProfileCompositionOptimizationStrategy select(@NonNull CompositionOptimizationAlgorithm algorithm);
}
