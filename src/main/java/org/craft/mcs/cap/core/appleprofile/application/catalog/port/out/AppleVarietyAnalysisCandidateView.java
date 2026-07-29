package org.craft.mcs.cap.core.appleprofile.application.catalog.port.out;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Immutable catalog projection used as input by profile-analysis scenarios.
 */
public record AppleVarietyAnalysisCandidateView(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName name,
    @NonNull AppleVarietyReferenceProfile referenceProfile
) {

  public AppleVarietyAnalysisCandidateView {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(name, "apple variety name cannot be null");
    requireNonNull(referenceProfile, "apple variety reference profile cannot be null");
  }
}
