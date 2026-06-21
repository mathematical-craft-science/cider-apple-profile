package org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain;

import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileComposition;
import org.jspecify.annotations.NonNull;


/**
 * Optional objective criterion used to rank composition candidates beyond target distance.
 *
 * <p>Criteria are deliberately separated from profile-target distance: distance describes geometric
 * target fit on sugar/acidity/tannin axes, while criteria model production-readiness concerns such
 * as YAN. Optimization strategies can evaluate optional criteria without learning
 * criterion-specific rules.
 */
@FunctionalInterface
public interface CompositionOptimizationCriterion {

  @NonNull
  CompositionOptimizationCriterionAssessment assess(@NonNull ChemicalProfileComposition composition);
}
