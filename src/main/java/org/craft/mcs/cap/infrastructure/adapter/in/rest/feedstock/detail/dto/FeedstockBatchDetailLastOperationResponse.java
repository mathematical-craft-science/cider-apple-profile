package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.jspecify.annotations.NonNull;


/**
 * Transport representation of the last current-profile operation known to the detail read model.
 */
@Schema(
    description = "Reserved legacy shape. Measurements are no longer modeled as production operations.",
    deprecated = true
)
public record FeedstockBatchDetailLastOperationResponse(
    @Schema(
        description = "Legacy operation type.",
        implementation = FeedstockBatchCurrentProfileOperationType.class,
        deprecated = true,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockBatchCurrentProfileOperationType type,
    @Schema(
        description = "Legacy occurrence time.",
        format = "date-time",
        deprecated = true,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant occurredAt
) {
}
