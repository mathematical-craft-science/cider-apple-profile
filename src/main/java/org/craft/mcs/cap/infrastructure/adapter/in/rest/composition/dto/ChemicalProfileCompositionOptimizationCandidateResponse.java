package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Single optimized chemical profile composition candidate.")
public record ChemicalProfileCompositionOptimizationCandidateResponse(
    @ArraySchema(schema = @Schema(description = "Source contribution.", implementation = ChemicalProfileCompositionSourceResponse.class))
    @NonNull List<@NonNull ChemicalProfileCompositionSourceResponse> sources,
    @Schema(description = "Calculated profile point.")
    @NonNull SugarAcidityProfilePointResponse profile,
    @Schema(description = "Total candidate volume in liters, when known.", example = "100.0")
    @Nullable BigDecimal totalVolumeLiters,
    @ArraySchema(schema = @Schema(description = "Composition warning.", implementation = ChemicalProfileCompositionWarning.class))
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings,
    @Schema(description = "Distance from candidate profile to target.")
    @NonNull ProfileTargetDistanceResponse targetDistance
) {
}
