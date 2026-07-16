package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Calculated chemical profile composition response.")
public record ChemicalProfileCompositionResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Composition source results.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ChemicalProfileCompositionSourceResponse.class)
    )
    @NonNull List<@NonNull ChemicalProfileCompositionSourceResponse> sources,
    @Schema(description = "Calculated profile point.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityProfilePointResponse profile,
    @Schema(description = "Calculated yeast-assimilable nitrogen in mg N/L, when every source contains YAN.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable SugarAcidityAxisPointResponse yeastAssimilableNitrogen,
    @Schema(description = "Total composition volume, when known.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable DecimalQuantityResponse totalVolume,
    @ArraySchema(
        arraySchema = @Schema(description = "Composition warnings.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ChemicalProfileCompositionWarning.class)
    )
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings,
    @Schema(description = "Normalizer used for target distance.", implementation = ProfileAxisNormalizerCode.class, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileAxisNormalizerCode normalizerCode,
    @Schema(description = "Distance to target, when target was provided.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileTargetDistanceResponse targetDistance
) {
}
