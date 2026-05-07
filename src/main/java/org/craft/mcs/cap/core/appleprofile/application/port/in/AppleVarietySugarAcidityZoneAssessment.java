package org.craft.mcs.cap.core.appleprofile.application.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessment;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietySugarAcidityZoneAssessment(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName varietyName,
    @NonNull SugarAcidityZoneAssessment assessment
) {

  public AppleVarietySugarAcidityZoneAssessment {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(assessment, "sugar-acidity zone assessment cannot be null");
  }
}
