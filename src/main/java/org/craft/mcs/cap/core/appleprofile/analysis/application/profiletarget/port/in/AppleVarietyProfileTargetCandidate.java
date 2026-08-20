package org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in;

import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleContributionView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetCharacteristics;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDistanceView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietyProfileTargetCandidate(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyAnalysisName varietyName,
    @NonNull AppleTargetCharacteristics characteristics,
    @NonNull AppleTargetDistanceView distance,
    @NonNull AppleContributionView contributionRole
) {

  public AppleVarietyProfileTargetCandidate {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(characteristics, "apple variety target-scoring characteristics cannot be null");
    requireNonNull(distance, "profile target distance cannot be null");
    requireNonNull(contributionRole, "composition contribution role cannot be null");
  }
}
