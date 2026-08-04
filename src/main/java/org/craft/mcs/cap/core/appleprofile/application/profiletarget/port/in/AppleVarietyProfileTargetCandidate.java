package org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietyProfileTargetCandidate(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName varietyName,
    @NonNull ProfileTargetScoringInput characteristics,
    @NonNull ProfileTargetDistance distance,
    @NonNull CompositionContributionRoleAssessment contributionRole
) {

  public AppleVarietyProfileTargetCandidate {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(characteristics, "apple variety target-scoring characteristics cannot be null");
    requireNonNull(distance, "profile target distance cannot be null");
    requireNonNull(contributionRole, "composition contribution role cannot be null");
  }
}
