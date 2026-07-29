package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain;

import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.CompositionOptimizationObjectiveAssessment;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CalculatedComposition;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.jspecify.annotations.NonNull;
import static java.util.Objects.requireNonNull;


/**
 * One optimized candidate returned by an optimization strategy.
 *
 * <p>The candidate contains the calculated composition, pure target distance and full objective
 * assessment. The distance explains SG/TA/tannin target fit. The objective assessment adds
 * optional production-readiness penalties such as YAN and is used for ranking.
 */
public record CompositionOptimizationCandidate(
    @NonNull CalculatedComposition composition,
    @NonNull ProfileTargetDistance distance,
    @NonNull CompositionOptimizationObjectiveAssessment objectiveAssessment
) {

  public CompositionOptimizationCandidate {
    requireNonNull(composition, "composition cannot be null");
    requireNonNull(distance, "profile target distance cannot be null");
    requireNonNull(objectiveAssessment, "composition optimization objective assessment cannot be null");
  }
}
