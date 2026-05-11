package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Batch of apple variety sugar-acidity graph points split by plottable and unplottable profiles.")
public record AppleVarietySugarAcidityZonePointBatchResponse(
    @ArraySchema(schema = @Schema(description = "Profiles that can be plotted on the sugar-acidity graph.", implementation = AppleVarietySugarAcidityZoneResponse.class))
    @NonNull List<@NonNull AppleVarietySugarAcidityZoneResponse> points,
    @ArraySchema(schema = @Schema(description = "Profiles missing required measurements for plotting.", implementation = UnplottableAppleVarietySugarAcidityZonePointResponse.class))
    @NonNull List<@NonNull UnplottableAppleVarietySugarAcidityZonePointResponse> unplottable,
    @Schema(description = "Opaque cursor for the next page. Omitted when the current page is the last one.")
    @Nullable String nextCursor
) {
}
