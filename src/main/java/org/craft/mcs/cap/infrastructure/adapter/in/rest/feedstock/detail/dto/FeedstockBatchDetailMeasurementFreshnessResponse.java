package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentMeasurementFreshnessStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Transport freshness signal for the latest measurement used as current profile evidence.
 */
@Schema(description = "Freshness assessment for the latest feedstock measurement.")
public record FeedstockBatchDetailMeasurementFreshnessResponse(
    @Schema(
        description = "Freshness status of latest measurement.",
        implementation = FeedstockCurrentMeasurementFreshnessStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockCurrentMeasurementFreshnessStatus status,
    @Schema(description = "Whether the latest measurement is stale.", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean stale,
    @Schema(
        description = "Reason when the measurement is stale.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String reason
) {
}
