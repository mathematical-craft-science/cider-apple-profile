package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetScoringCharacteristicsResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Feedstock batch current profile score against a target.")
public record FeedstockBatchProfileTargetScoreResponse(
    @Schema(description = "Feedstock batch identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID batchId,
    @Schema(description = "Workspace identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID workspaceId,
    @Schema(
        description = "Material state whose current characteristics were scored.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMaterialStateId,
    @Schema(
        description = "Latest measurement set contributing to the composite current profile.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMeasurementId,
    @Schema(
        description = "Name of the latest measurement set contributing to the composite current profile.",
        example = "Pressing measurement #1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String currentMeasurementName,
    @Schema(description = "Current target-scoring characteristics.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetScoringCharacteristicsResponse characteristics,
    @Schema(description = "Distance to target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetDistanceResponse distance
) {
}
