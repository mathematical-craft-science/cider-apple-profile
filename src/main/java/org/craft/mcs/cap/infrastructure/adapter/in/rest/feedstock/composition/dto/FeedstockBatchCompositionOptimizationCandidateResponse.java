package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CalculatedCompositionCharacteristicsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationObjectiveAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Single optimized feedstock batch composition candidate.")
public record FeedstockBatchCompositionOptimizationCandidateResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Source contribution.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockBatchCompositionOptimizationSourceResponse.class)
    )
    @NonNull List<@NonNull FeedstockBatchCompositionOptimizationSourceResponse> sources,
    @Schema(description = "Calculated composition characteristics.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CalculatedCompositionCharacteristicsResponse characteristics,
    @Schema(
        description = "Total candidate volume, when known.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse totalVolume,
    @ArraySchema(
        arraySchema = @Schema(description = "Composition warnings.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ChemicalProfileCompositionWarning.class)
    )
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings,
    @Schema(description = "Distance from candidate profile to target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetDistanceResponse targetDistance,
    @Schema(
        description = "Full objective assessment used to rank this candidate.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionOptimizationObjectiveAssessmentResponse objectiveAssessment
) {
}
