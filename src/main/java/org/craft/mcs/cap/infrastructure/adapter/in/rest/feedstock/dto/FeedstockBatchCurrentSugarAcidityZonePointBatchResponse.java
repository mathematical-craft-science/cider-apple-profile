package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Current feedstock batch sugar-acidity graph point batch response.")
public record FeedstockBatchCurrentSugarAcidityZonePointBatchResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Plottable current batch graph points.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockBatchCurrentSugarAcidityZoneResponse.class)
    )
    @NonNull List<@NonNull FeedstockBatchCurrentSugarAcidityZoneResponse> points,
    @ArraySchema(
        arraySchema = @Schema(description = "Current batch profiles that cannot be plotted because required measurements are missing.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = UnplottableFeedstockBatchCurrentSugarAcidityZonePointResponse.class)
    )
    @NonNull List<@NonNull UnplottableFeedstockBatchCurrentSugarAcidityZonePointResponse> unplottable,
    @Schema(description = "Opaque cursor for the next page. Omitted when the current page is the last one.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String nextCursor
) {
}
