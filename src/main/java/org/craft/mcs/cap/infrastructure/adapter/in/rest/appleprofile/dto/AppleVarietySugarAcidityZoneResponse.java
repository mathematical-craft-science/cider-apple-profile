package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityZoneAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record AppleVarietySugarAcidityZoneResponse(
    @NonNull String varietyId,
    @NonNull String varietyName,
    boolean plottable,
    @Nullable SugarAcidityZoneAssessmentResponse assessment,
    @Nullable ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {

  public AppleVarietySugarAcidityZoneResponse(
      @NonNull String varietyId,
      @NonNull String varietyName,
      @NonNull SugarAcidityZoneAssessmentResponse assessment
  ) {
    this(varietyId, varietyName, true, assessment, null);
  }
}
