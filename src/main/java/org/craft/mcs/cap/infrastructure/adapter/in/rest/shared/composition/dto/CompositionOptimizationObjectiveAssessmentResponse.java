package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Full objective assessment used to rank a composition optimization candidate.")
public record CompositionOptimizationObjectiveAssessmentResponse(
    @Schema(description = "Final objective score used for ranking. It starts with target distance and adds optional penalties.", example = "0.125", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal score,
    @ArraySchema(
        arraySchema = @Schema(description = "Objective penalties added to target distance.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = CompositionOptimizationPenaltyResponse.class)
    )
    @NonNull List<@NonNull CompositionOptimizationPenaltyResponse> penalties
) {
}
