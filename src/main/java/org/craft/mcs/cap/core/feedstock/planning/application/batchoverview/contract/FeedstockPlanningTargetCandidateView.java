package org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.contract;

import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockContributionView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetCharacteristics;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetDistanceView;
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
    @NonNull FeedstockTargetCharacteristics characteristics,
    @NonNull FeedstockTargetDistanceView distance,
    @NonNull FeedstockContributionView contributionRole
) {

  public FeedstockPlanningTargetCandidateView {
    requireNonNull(currentMaterialStateId, "current material state id cannot be null");
    requireNonNull(characteristics, "planning target-scoring characteristics cannot be null");
    requireNonNull(distance, "profile target distance cannot be null");
    requireNonNull(contributionRole, "composition contribution role cannot be null");
  }
}
