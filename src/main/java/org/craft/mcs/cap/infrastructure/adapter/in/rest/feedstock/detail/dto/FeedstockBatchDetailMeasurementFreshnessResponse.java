package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Transport freshness summary for independently selected current-profile evidence.
 */
@Schema(description = "Aggregate freshness assessment for evidence used by the current feedstock profile.")
public record FeedstockBatchDetailMeasurementFreshnessResponse(
    @Schema(
        description = "Worst freshness status among planning-relevant current evidence.",
        implementation = MeasurementFreshnessStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementFreshnessStatus status,
    @Schema(
        description = "Whether at least one planning-relevant current evidence value is stale.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean stale,
    @Schema(
        description = "Reason when current-profile evidence is stale.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String reason
) {
}
