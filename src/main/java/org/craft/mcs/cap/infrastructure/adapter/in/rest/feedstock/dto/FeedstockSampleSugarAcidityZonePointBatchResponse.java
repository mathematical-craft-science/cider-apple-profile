package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Feedstock sample sugar-acidity graph point batch response.")
public record FeedstockSampleSugarAcidityZonePointBatchResponse(
    @ArraySchema(schema = @Schema(description = "Plottable graph points.", implementation = FeedstockSampleSugarAcidityZoneResponse.class))
    @NonNull List<@NonNull FeedstockSampleSugarAcidityZoneResponse> points,
    @ArraySchema(schema = @Schema(description = "Samples that cannot be plotted because required measurements are missing.", implementation = UnplottableFeedstockSampleSugarAcidityZonePointResponse.class))
    @NonNull List<@NonNull UnplottableFeedstockSampleSugarAcidityZonePointResponse> unplottable
) {
}
