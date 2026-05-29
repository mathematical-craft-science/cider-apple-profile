package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Feedstock batch list response.")
public record FeedstockBatchListResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Feedstock batches.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockBatchResponse.class)
    )
    @NonNull List<@NonNull FeedstockBatchResponse> batches
) {
}
