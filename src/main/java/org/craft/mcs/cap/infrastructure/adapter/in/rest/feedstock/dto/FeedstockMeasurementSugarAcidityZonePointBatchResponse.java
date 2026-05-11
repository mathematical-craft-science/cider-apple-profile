package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock measurement sugar-acidity graph point batch response.")
public record FeedstockMeasurementSugarAcidityZonePointBatchResponse(
    @ArraySchema(schema = @Schema(description = "Plottable graph points.", implementation = FeedstockMeasurementSugarAcidityZoneResponse.class))
    @NonNull List<@NonNull FeedstockMeasurementSugarAcidityZoneResponse> points,
    @ArraySchema(schema = @Schema(description = "Measurements that cannot be plotted because required measurements are missing.", implementation = UnplottableFeedstockMeasurementSugarAcidityZonePointResponse.class))
    @NonNull List<@NonNull UnplottableFeedstockMeasurementSugarAcidityZonePointResponse> unplottable,
    @Schema(description = "Opaque cursor for the next page. Omitted when the current page is the last one.")
    @Nullable String nextCursor
) {
}
