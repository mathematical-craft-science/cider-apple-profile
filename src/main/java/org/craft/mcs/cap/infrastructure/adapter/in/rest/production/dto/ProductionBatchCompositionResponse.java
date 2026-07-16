package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Production batch calculated chemical profile composition.")
public record ProductionBatchCompositionResponse(
    @ArraySchema(
        arraySchema = @Schema(
            description = "Production source contributions.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = ProductionBatchSourceResponse.class)
    )
    @NonNull List<@NonNull ProductionBatchSourceResponse> sources,
    @Schema(description = "Calculated production profile point.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityProfilePointResponse profile,
    @Schema(
        description = "Yeast-assimilable nitrogen from the accepted production profile, when available.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse yeastAssimilableNitrogen,
    @Schema(description = "Total production volume.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse totalVolume,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Composition calculation warnings.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = ChemicalProfileCompositionWarning.class)
    )
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings,
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
