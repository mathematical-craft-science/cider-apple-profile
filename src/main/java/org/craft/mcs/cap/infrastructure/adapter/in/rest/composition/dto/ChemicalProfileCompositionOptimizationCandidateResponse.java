package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Single optimized chemical profile composition candidate.")
public record ChemicalProfileCompositionOptimizationCandidateResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Source contribution.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ChemicalProfileCompositionSourceResponse.class)
    )
    @NonNull List<@NonNull ChemicalProfileCompositionSourceResponse> sources,
    @Schema(description = "Calculated profile point.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityProfilePointResponse profile,
    @Schema(description = "Calculated yeast-assimilable nitrogen in mg N/L, when every source contains YAN.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable SugarAcidityAxisPointResponse yeastAssimilableNitrogen,
    @Schema(description = "Total candidate volume in liters, when known.", example = "100.0", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal totalVolumeLiters,
    @ArraySchema(
        arraySchema = @Schema(description = "Composition warnings.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ChemicalProfileCompositionWarning.class)
    )
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings,
    @Schema(description = "Distance from candidate profile to target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetDistanceResponse targetDistance
) {
}
