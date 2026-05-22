package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.jspecify.annotations.NonNull;


public record FeedstockBatchCurrentFermentationStartReadinessResponse(
    @Schema(format = "uuid") @NonNull UUID batchId,
    @Schema(format = "uuid") @NonNull UUID ciderMakerId,
    @Schema(format = "uuid") @NonNull UUID currentMeasurementId,
    @NonNull String currentMeasurementName,
    @NonNull FermentationStartReadinessAssessmentResponse assessment
) {
}
