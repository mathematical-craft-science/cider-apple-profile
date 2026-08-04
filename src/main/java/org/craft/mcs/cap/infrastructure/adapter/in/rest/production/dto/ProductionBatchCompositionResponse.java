package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionWarning;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CalculatedCompositionCharacteristicsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Calculated composition characteristics accepted by the production candidate.")
public record ProductionBatchCompositionResponse(
    @ArraySchema(
        arraySchema = @Schema(
            description = "Production source contributions.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = ProductionBatchSourceResponse.class)
    )
    @NonNull List<@NonNull ProductionBatchSourceResponse> sources,
    @Schema(description = "Calculated production characteristics.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CalculatedCompositionCharacteristicsResponse characteristics,
    @Schema(
        description = "Potential ABV prediction saved with the authoritative production candidate snapshot.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable PotentialAbvPredictionResponse potentialAbv,
    @Schema(description = "Total production volume.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse totalVolume,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Composition calculation warnings.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = CompositionWarning.class)
    )
    @NonNull List<@NonNull CompositionWarning> warnings,
    @Schema(
        description = "Normalizer used for target assessment, when a target was supplied.",
        implementation = ProfileAxisNormalizerCode.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileAxisNormalizerCode normalizerCode,
    @Schema(
        description = "Authoritative target assessment of the saved candidate, when a target was supplied.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileTargetDistanceResponse targetDistance
) {
}
