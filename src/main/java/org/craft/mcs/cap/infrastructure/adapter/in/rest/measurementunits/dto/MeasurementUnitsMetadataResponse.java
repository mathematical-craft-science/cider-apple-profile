package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


/**
 * Collection response for canonical measurement unit metadata.
 *
 * <p>This DTO is a stable backend contract for reusable UI configuration, not a screen-specific
 * view model. Consumers can select the unit entries they need for feedstock, production, or
 * analysis workflows.
 */
@Schema(description = "Canonical measurement unit metadata collection.")
public record MeasurementUnitsMetadataResponse(
    @ArraySchema(
        arraySchema = @Schema(
            description = "Canonical measurement units.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementUnitMetadataResponse.class)
    )
    @NonNull List<@NonNull MeasurementUnitMetadataResponse> units,
    @Schema(
        description = "Freshness policy for current-profile measurement evidence.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementFreshnessMetadataResponse measurementFreshness
) {
}
