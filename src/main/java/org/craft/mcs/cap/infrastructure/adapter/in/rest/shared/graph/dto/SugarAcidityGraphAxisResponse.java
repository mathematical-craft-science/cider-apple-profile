package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Sugar-acidity graph axis metadata.")
public record SugarAcidityGraphAxisResponse(
    @Schema(
        description = "Canonical unit of every value on this axis.",
        example = "SG",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String unit,
    @Schema(
        description = "Minimum axis value.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse min,
    @Schema(
        description = "Maximum axis value.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse max,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Ordered boundaries of the configured Jolicoeur graph interpretation.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = DecimalQuantityResponse.class)
    )
    @NonNull List<@NonNull DecimalQuantityResponse> boundaryValues
) {
}
