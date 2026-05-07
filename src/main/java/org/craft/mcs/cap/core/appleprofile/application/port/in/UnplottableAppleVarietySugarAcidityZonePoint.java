package org.craft.mcs.cap.core.appleprofile.application.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessment;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record UnplottableAppleVarietySugarAcidityZonePoint(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName varietyName,
    @NonNull ChemicalProfileCapabilityAssessment capabilityAssessment
) {

  public UnplottableAppleVarietySugarAcidityZonePoint {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(capabilityAssessment, "chemical profile capability assessment cannot be null");
  }
}
