package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Aggregate freshness assessment of evidence selected for the current feedstock profile.")
public record FeedstockCurrentProfileFreshnessResponse(
    @Schema(
        description = "Freshness status.",
        implementation = MeasurementFreshnessStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementFreshnessStatus status,
    @Schema(
        description = "Whether any planning-required selected evidence is stale.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean stale,
    @Schema(
        description = "Reason when selected profile evidence is stale.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String reason
) {
}
