package org.craft.mcs.cap.core.appleprofile.application.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneAssessment;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record AppleVarietySugarAcidityZoneResult(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName varietyName,
    boolean plottable,
    @Nullable SugarAcidityZoneAssessment assessment,
    @NonNull ChemicalProfileCapabilityAssessment capabilityAssessment
) {

  public AppleVarietySugarAcidityZoneResult {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(capabilityAssessment, "chemical profile capability assessment cannot be null");

    if (plottable && assessment == null) {
      throw new IllegalArgumentException("plottable apple variety result must contain assessment");
    }
  }
}
