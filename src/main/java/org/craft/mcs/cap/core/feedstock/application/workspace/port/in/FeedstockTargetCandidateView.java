package org.craft.mcs.cap.core.feedstock.application.workspace.port.in;

import org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockTargetCandidateView(
    @NonNull SugarAcidityProfilePoint point,
    @NonNull ProfileTargetDistance distance,
    @NonNull CompositionContributionRoleAssessment contributionRole
) {

  public FeedstockTargetCandidateView {
    requireNonNull(point, "sugar-acidity profile point cannot be null");
    requireNonNull(distance, "profile target distance cannot be null");
    requireNonNull(contributionRole, "composition contribution role cannot be null");
  }
}
