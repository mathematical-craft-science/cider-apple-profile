package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;


public record FeedstockBatchProfileTargetScoreResponse(
    @NonNull UUID batchId,
    @NonNull UUID ciderMakerId,
    @NonNull UUID currentMeasurementId,
    @NonNull String currentMeasurementName,
    @NonNull SugarAcidityProfilePointResponse point,
    @NonNull ProfileTargetDistanceResponse distance
) {
}
