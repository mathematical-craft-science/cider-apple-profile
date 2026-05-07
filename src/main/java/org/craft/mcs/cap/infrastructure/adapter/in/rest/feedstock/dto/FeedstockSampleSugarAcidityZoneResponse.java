package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityZoneAssessmentResponse;
import org.jspecify.annotations.NonNull;


public record FeedstockSampleSugarAcidityZoneResponse(
    @NonNull String sampleId,
    @NonNull String ciderMakerId,
    @NonNull String sampleName,
    @NonNull SugarAcidityZoneAssessmentResponse assessment
) {
}
