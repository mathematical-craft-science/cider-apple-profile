package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionWarning;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CalculatedCompositionCharacteristicsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationObjectiveAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Single optimized composition candidate.")
public record CompositionOptimizationCandidateResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Source contribution.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = CompositionSourceResponse.class)
    )
    @NonNull List<@NonNull CompositionSourceResponse> sources,
    @Schema(description = "Calculated composition characteristics.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CalculatedCompositionCharacteristicsResponse characteristics,
    @Schema(
        description = "Potential ABV predicted from the candidate SG.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull PotentialAbvPredictionResponse potentialAbv,
    @Schema(
        description = "Total candidate volume, when known.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse totalVolume,
    @ArraySchema(
        arraySchema = @Schema(description = "Composition warnings.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = CompositionWarning.class)
    )
    @NonNull List<@NonNull CompositionWarning> warnings,
    @Schema(description = "Distance from candidate profile to target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetDistanceResponse targetDistance,
    @Schema(
        description = "Full objective assessment used to rank this candidate.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionOptimizationObjectiveAssessmentResponse objectiveAssessment
) {
}
