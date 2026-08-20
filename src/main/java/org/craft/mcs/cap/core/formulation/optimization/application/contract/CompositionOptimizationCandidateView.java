package org.craft.mcs.cap.core.formulation.optimization.application.contract;

import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculatedCompositionView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationPotentialAbvView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Application view enriching a ranked composition candidate with its potential ABV prediction. */
public record CompositionOptimizationCandidateView(
    @NonNull CalculatedCompositionView composition,
    @NonNull FormulationTargetDistanceView targetDistance,
    @NonNull OptimizationObjectiveView objective,
    @NonNull FormulationPotentialAbvView potentialAbv
) {

  public CompositionOptimizationCandidateView {
    requireNonNull(composition, "optimized composition cannot be null");
    requireNonNull(targetDistance, "optimized composition target distance cannot be null");
    requireNonNull(objective, "optimized composition objective cannot be null");
    requireNonNull(potentialAbv, "composition optimization candidate potential ABV cannot be null");
  }
}
