package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessStatus;
import org.jspecify.annotations.NonNull;


@Schema(description = "Fermentation-start readiness assessment.")
public record FermentationStartReadinessAssessmentResponse(
    @Schema(description = "Overall readiness status.", implementation = FermentationStartReadinessStatus.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FermentationStartReadinessStatus status,
    @Schema(description = "Assessment method code.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String method,
    @ArraySchema(
        arraySchema = @Schema(description = "Readiness checks.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FermentationStartReadinessCheckResponse.class)
    )
    @NonNull List<@NonNull FermentationStartReadinessCheckResponse> checks
) {
}
