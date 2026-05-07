package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityZoneAssessmentResponse;
import org.jspecify.annotations.NonNull;


public record AppleVarietySugarAcidityZoneResponse(
    @NonNull String varietyId,
    @NonNull String varietyName,
    @NonNull SugarAcidityZoneAssessmentResponse assessment
) {
}
