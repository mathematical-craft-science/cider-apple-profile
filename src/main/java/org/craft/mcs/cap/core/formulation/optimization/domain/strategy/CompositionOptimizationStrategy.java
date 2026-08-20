package org.craft.mcs.cap.core.formulation.optimization.domain.strategy;

import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationResult;
import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationRequest;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionCalculator;
import org.jspecify.annotations.NonNull;


/**
 * Proposes candidate source weights for a target-characteristic objective.
 *
 * <p>The strategy does not own the chemistry calculation. It receives a
 * {@link CompositionCalculator} per call so the same optimization algorithm can be
 * reused with different calculators selected by the application layer. Each generated candidate is
 * calculated by the supplied calculator and scored by the objective function contained in the
 * request.
 */
public interface CompositionOptimizationStrategy {

  @NonNull
  CompositionOptimizationResult optimize(
      @NonNull CompositionOptimizationRequest request,
      @NonNull CompositionCalculator calculator
  );
}
