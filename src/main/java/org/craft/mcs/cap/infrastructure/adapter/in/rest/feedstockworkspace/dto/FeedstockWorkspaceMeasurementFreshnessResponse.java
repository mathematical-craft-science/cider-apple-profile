package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Freshness assessment for the current feedstock measurement.")
public record FeedstockWorkspaceMeasurementFreshnessResponse(
    @Schema(
        description = "Freshness status.",
        implementation = MeasurementFreshnessStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementFreshnessStatus status,
    @Schema(description = "Whether the current measurement is stale.", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean stale,
    @Schema(
        description = "Reason when the measurement is stale.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String reason
) {
}
