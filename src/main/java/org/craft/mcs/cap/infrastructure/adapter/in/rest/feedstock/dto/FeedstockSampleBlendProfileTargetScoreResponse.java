package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;


public record FeedstockSampleBlendProfileTargetScoreResponse(
    @NonNull UUID sampleId,
    @NonNull UUID ciderMakerId,
    @NonNull UUID batchId,
    @NonNull String sampleName,
    @NonNull SugarAcidityProfilePointResponse point,
    @NonNull BlendProfileTargetDistanceResponse distance
) {
}
