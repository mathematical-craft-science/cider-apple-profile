package org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.model;

import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Target-aware candidate assessment attached to a feedstock planning row.
 *
 * <p>It combines typed scoring characteristics, target distance and contribution role so a client can show
 * why this batch may be useful for a selected production-planning target.
 */
public record FeedstockPlanningTargetCandidateView(
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull ProfileTargetScoringInput characteristics,
    @NonNull ProfileTargetDistance distance,
    @NonNull CompositionContributionRoleAssessment contributionRole
) {

  public FeedstockPlanningTargetCandidateView {
    requireNonNull(currentMaterialStateId, "current material state id cannot be null");
    requireNonNull(characteristics, "planning target-scoring characteristics cannot be null");
    requireNonNull(distance, "profile target distance cannot be null");
    requireNonNull(contributionRole, "composition contribution role cannot be null");
  }
}
