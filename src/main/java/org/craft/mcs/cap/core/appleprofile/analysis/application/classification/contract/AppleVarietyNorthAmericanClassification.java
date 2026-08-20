package org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract;

import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietyNorthAmericanClassification(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyAnalysisName varietyName,
    @NonNull AppleVarietyNorthAmericanAssessmentView profile
) {

  public AppleVarietyNorthAmericanClassification {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(profile, "north american profile cannot be null");
    if (profile.sugar() == null || profile.acidity() == null || profile.tannin() == null) {
      throw new ApplicationValidationException("North American classification requires a complete profile");
    }
  }
}
