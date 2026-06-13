package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock measurement sugar-acidity graph point batch response.")
public record FeedstockMeasurementSugarAcidityZonePointBatchResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Plottable graph points.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockMeasurementSugarAcidityZoneResponse.class)
    )
    @NonNull List<@NonNull FeedstockMeasurementSugarAcidityZoneResponse> points,
    @ArraySchema(
        arraySchema = @Schema(description = "Measurements that cannot be plotted because required measurements are missing.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = UnplottableFeedstockMeasurementSugarAcidityZonePointResponse.class)
    )
    @NonNull List<@NonNull UnplottableFeedstockMeasurementSugarAcidityZonePointResponse> unplottable,
    @Schema(description = "Opaque cursor for the next page. Omitted when the current page is the last one.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String nextCursor
) {
}
