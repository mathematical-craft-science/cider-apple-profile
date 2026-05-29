package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Graph point coordinates computed from a chemical profile.")
public record SugarAcidityProfilePointResponse(
    @Schema(description = "Specific gravity coordinate.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull SugarAcidityAxisPointResponse sugar,
    @Schema(description = "Titratable acidity coordinate.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull SugarAcidityAxisPointResponse acidity,
    @Schema(description = "Tannin coordinate, when present.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable SugarAcidityAxisPointResponse tannin
) {
}
