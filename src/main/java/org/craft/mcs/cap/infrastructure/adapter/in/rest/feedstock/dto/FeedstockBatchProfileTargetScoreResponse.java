package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Feedstock batch current profile score against a target.")
public record FeedstockBatchProfileTargetScoreResponse(
    @Schema(description = "Feedstock batch identifier.", format = "uuid")
    @NonNull UUID batchId,
    @Schema(description = "Cider maker identifier.", format = "uuid")
    @NonNull UUID ciderMakerId,
    @Schema(description = "Current measurement identifier.", format = "uuid")
    @NonNull UUID currentMeasurementId,
    @Schema(description = "Current measurement name.", example = "Pressing measurement #1")
    @NonNull String currentMeasurementName,
    @Schema(description = "Current sugar-acidity profile point.")
    @NonNull SugarAcidityProfilePointResponse point,
    @Schema(description = "Distance to target.")
    @NonNull ProfileTargetDistanceResponse distance
) {
}
