package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Graph point coordinates computed from a chemical profile.")
public record SugarAcidityProfilePointResponse(
    @Schema(description = "Specific gravity coordinate.") @NonNull SugarAcidityAxisPointResponse sugar,
    @Schema(description = "Titratable acidity coordinate.") @NonNull SugarAcidityAxisPointResponse acidity,
    @Schema(description = "Tannin coordinate, when present.")
    @Nullable SugarAcidityAxisPointResponse tannin
) {
}
