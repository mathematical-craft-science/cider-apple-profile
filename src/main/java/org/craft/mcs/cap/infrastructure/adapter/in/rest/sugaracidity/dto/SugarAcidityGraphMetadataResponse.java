package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Sugar-acidity plot metadata for frontend graph rendering.")
public record SugarAcidityGraphMetadataResponse(
    @Schema(description = "Specific gravity axis metadata.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull SugarAcidityGraphAxisResponse sugarAxis,
    @Schema(description = "Titratable acidity axis metadata.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull SugarAcidityGraphAxisResponse acidityAxis,
    @ArraySchema(
        arraySchema = @Schema(description = "Graph zones with geometry required for rendering.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = SugarAcidityGraphZoneResponse.class)
    )
    @NonNull List<@NonNull SugarAcidityGraphZoneResponse> zones
) {
}
