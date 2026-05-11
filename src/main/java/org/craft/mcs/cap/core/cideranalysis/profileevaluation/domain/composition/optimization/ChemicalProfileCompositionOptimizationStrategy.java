package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculator;
import org.jspecify.annotations.NonNull;


/**
 * Proposes candidate source weights for a target chemical profile objective.
 *
 * <p>The strategy does not own the chemistry calculation. It receives a
 * {@link ChemicalProfileCompositionCalculator} per call so the same optimization algorithm can be
 * reused with different calculators selected by the application layer. Each generated candidate is
 * calculated by the supplied calculator and scored by the objective function contained in the
 * request.
 */
public interface ChemicalProfileCompositionOptimizationStrategy {

  @NonNull
  ChemicalProfileCompositionOptimizationResult optimize(
      @NonNull ChemicalProfileCompositionOptimizationRequest request,
      @NonNull ChemicalProfileCompositionCalculator calculator
  );
}
