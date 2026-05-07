package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.jspecify.annotations.NonNull;


public record UnplottableAppleVarietySugarAcidityZonePointResponse(
    @NonNull String varietyId,
    @NonNull String varietyName,
    @NonNull ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {
}
