package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;


public record FeedstockBatchBlendProfileTargetScoreResponse(
    @NonNull UUID batchId,
    @NonNull UUID ciderMakerId,
    @NonNull UUID currentMeasurementId,
    @NonNull String currentMeasurementName,
    @NonNull SugarAcidityProfilePointResponse point,
    @NonNull ProfileTargetDistanceResponse distance
) {
}
