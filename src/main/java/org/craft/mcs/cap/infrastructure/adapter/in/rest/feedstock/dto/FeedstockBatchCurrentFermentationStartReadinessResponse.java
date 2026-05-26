package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.jspecify.annotations.NonNull;


@Schema(description = "Current feedstock batch fermentation-start readiness response.")
public record FeedstockBatchCurrentFermentationStartReadinessResponse(
    @Schema(description = "Feedstock batch identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull UUID batchId,
    @Schema(description = "Cider maker identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull UUID ciderMakerId,
    @Schema(description = "Current feedstock measurement identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull UUID currentMeasurementId,
    @Schema(description = "Current measurement name.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String currentMeasurementName,
    @Schema(description = "Readiness assessment.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FermentationStartReadinessAssessmentResponse assessment
) {
}
