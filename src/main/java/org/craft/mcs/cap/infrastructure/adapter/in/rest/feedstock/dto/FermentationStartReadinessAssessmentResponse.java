package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessStatus;
import org.jspecify.annotations.NonNull;


public record FermentationStartReadinessAssessmentResponse(
    @Schema(implementation = FermentationStartReadinessStatus.class)
    @NonNull FermentationStartReadinessStatus status,
    @NonNull String method,
    @ArraySchema(schema = @Schema(implementation = FermentationStartReadinessCheckResponse.class))
    @NonNull List<@NonNull FermentationStartReadinessCheckResponse> checks
) {
}
