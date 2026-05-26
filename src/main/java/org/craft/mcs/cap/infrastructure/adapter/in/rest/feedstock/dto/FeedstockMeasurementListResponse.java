package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Feedstock measurement list response.")
public record FeedstockMeasurementListResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Feedstock measurements.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockMeasurementResponse.class)
    )
    @NonNull List<@NonNull FeedstockMeasurementResponse> measurements
) {
}
