package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Feedstock sample list response.")
public record FeedstockSampleListResponse(
    @ArraySchema(schema = @Schema(description = "Feedstock samples.", implementation = FeedstockSampleResponse.class))
    @NonNull List<@NonNull FeedstockSampleResponse> samples
) {
}
