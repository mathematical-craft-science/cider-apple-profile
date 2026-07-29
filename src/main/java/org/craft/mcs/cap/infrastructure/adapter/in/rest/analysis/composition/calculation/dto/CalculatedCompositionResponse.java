package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionWarning;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CalculatedCompositionCharacteristicsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Calculated composition response.")
public record CalculatedCompositionResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Composition source results.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = CompositionSourceResponse.class)
    )
    @NonNull List<@NonNull CompositionSourceResponse> sources,
    @Schema(description = "Calculated composition characteristics.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CalculatedCompositionCharacteristicsResponse characteristics,
    @Schema(
        description = "Potential ABV predicted from the calculated composition SG.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull PotentialAbvPredictionResponse potentialAbv,
    @Schema(
        description = "Total composition volume, when known.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse totalVolume,
    @ArraySchema(
        arraySchema = @Schema(description = "Composition warnings.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = CompositionWarning.class)
    )
    @NonNull List<@NonNull CompositionWarning> warnings,
    @Schema(
        description = "Normalizer used for target distance.",
        implementation = ProfileAxisNormalizerCode.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileAxisNormalizerCode normalizerCode,
    @Schema(
        description = "Distance to target, when target was provided.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileTargetDistanceResponse targetDistance
) {

}
