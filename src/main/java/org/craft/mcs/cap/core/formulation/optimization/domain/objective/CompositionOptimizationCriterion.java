package org.craft.mcs.cap.core.formulation.optimization.domain.objective;

import org.craft.mcs.cap.core.formulation.calculation.domain.CalculatedComposition;
import org.jspecify.annotations.NonNull;


/**
 * Optional objective criterion used to rank composition candidates beyond target distance.
 *
 * <p>Criteria are deliberately separated from profile-target distance: distance describes geometric
 * target fit on Specific Gravity, Titratable Acidity and optional tannin axes, while criteria model
 * production-readiness concerns such
 * as YAN. Optimization strategies can evaluate optional criteria without learning
 * criterion-specific rules.
 */
@FunctionalInterface
public interface CompositionOptimizationCriterion {

  @NonNull
  CompositionOptimizationCriterionAssessment assess(@NonNull CalculatedComposition composition);
}
