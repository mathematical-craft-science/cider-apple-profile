package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.jspecify.annotations.NonNull;


public record UnplottableFeedstockSampleSugarAcidityZonePointResponse(
    @NonNull String sampleId,
    @NonNull String ciderMakerId,
    @NonNull String batchId,
    @NonNull String sampleName,
    @NonNull ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {
}
