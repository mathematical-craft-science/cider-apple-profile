package org.craft.mcs.cap.core.appleprofile.application.catalog.port.out;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Immutable catalog projection used as input by profile-analysis scenarios.
 */
public record AppleVarietyAnalysisCandidateView(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName name,
    @NonNull ChemicalProfile chemicalProfile
) {

  public AppleVarietyAnalysisCandidateView {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(name, "apple variety name cannot be null");
    requireNonNull(chemicalProfile, "chemical profile cannot be null");
  }
}
