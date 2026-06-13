package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Current feedstock batch sugar-acidity graph point batch response.")
public record FeedstockBatchCurrentSugarAcidityZonePointBatchResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Current feedstock batch graph points. Unplottable batches are included with plottable=false and missingAxes.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockBatchGraphPointResponse.class)
    )
    @NonNull List<@NonNull FeedstockBatchGraphPointResponse> points,
    @Schema(description = "Opaque cursor for the next page. Omitted when the current page is the last one.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String nextCursor
) {
}
