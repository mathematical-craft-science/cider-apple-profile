package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.FeedstockBatchCurrentProfileOperationType;
import org.jspecify.annotations.NonNull;


/**
 * Transport representation of the last current-profile operation known to the detail read model.
 */
@Schema(description = "Last known operation reflected by the current profile.")
public record FeedstockBatchDetailLastOperationResponse(
    @Schema(
        description = "Operation type. Initially only MEASURED is emitted until material operations are recorded.",
        implementation = FeedstockBatchCurrentProfileOperationType.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockBatchCurrentProfileOperationType type,
    @Schema(
        description = "Time when the operation occurred.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant occurredAt
) {
}
