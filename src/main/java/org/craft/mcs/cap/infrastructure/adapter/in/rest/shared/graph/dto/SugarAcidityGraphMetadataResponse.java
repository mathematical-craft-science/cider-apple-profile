package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Jolicoeur sugar-acidity graph metadata.")
public record SugarAcidityGraphMetadataResponse(
    @Schema(
        description = "Specific Gravity (SG) axis metadata.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull SugarAcidityGraphAxisResponse specificGravityAxis,
    @Schema(
        description = "Titratable Acidity (TA) axis metadata.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull SugarAcidityGraphAxisResponse titratableAcidityAxis,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Jolicoeur zone geometry expressed in canonical SG and TA coordinates.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = SugarAcidityGraphZoneResponse.class)
    )
    @NonNull List<@NonNull SugarAcidityGraphZoneResponse> zones
) {
}
