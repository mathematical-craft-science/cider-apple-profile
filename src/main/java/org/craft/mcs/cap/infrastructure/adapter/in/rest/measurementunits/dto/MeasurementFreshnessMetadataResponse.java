package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;


/**
 * Metadata for latest-measurement freshness assessment used by feedstock read models.
 */
@Schema(description = "Latest measurement freshness metadata.")
public record MeasurementFreshnessMetadataResponse(
    @Schema(
        description = "ISO-8601 duration after which latest measurement evidence is considered stale.",
        example = "PT336H",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String staleAfter
) {
}
