package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model;

import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Target-aware candidate assessment attached to a feedstock planning row.
 *
 * <p>It combines the scorable point, target distance and contribution role so a client can show
 * why this batch may be useful for a selected production-planning target.
 */
public record FeedstockPlanningTargetCandidateView(
    @NonNull SugarAcidityProfilePoint point,
    @NonNull ProfileTargetDistance distance,
    @NonNull CompositionContributionRoleAssessment contributionRole,
    @NonNull FeedstockPlanningRoleExplanationView roleExplanation
) {

  public FeedstockPlanningTargetCandidateView {
    requireNonNull(point, "sugar-acidity profile point cannot be null");
    requireNonNull(distance, "profile target distance cannot be null");
    requireNonNull(contributionRole, "composition contribution role cannot be null");
    requireNonNull(roleExplanation, "feedstock planning role explanation cannot be null");
  }
}
