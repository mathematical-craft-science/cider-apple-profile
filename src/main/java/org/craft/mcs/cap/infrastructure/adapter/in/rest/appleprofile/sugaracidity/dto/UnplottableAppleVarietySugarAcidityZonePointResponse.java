package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.GraphMissingCharacteristic;
import org.jspecify.annotations.NonNull;


@Schema(
    description = "Apple variety that cannot be plotted because required profile characteristics are missing."
)
public record UnplottableAppleVarietySugarAcidityZonePointResponse(
    @Schema(
        description = "Apple variety identifier.",
        format = "uuid",
        example = "6d62d888-2a2b-4f47-8f21-2ab2d54d8c2f",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID varietyId,
    @Schema(description = "Apple variety name.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String varietyName,
    @ArraySchema(
        arraySchema = @Schema(
            description = "SG or TA inputs preventing graph projection.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = GraphMissingCharacteristic.class)
    )
    @NonNull List<@NonNull GraphMissingCharacteristic> missingCharacteristics
) {
}
