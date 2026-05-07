package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import java.util.List;
import org.jspecify.annotations.NonNull;


public record AppleVarietySugarAcidityZonePointBatchResponse(
    @NonNull List<@NonNull AppleVarietySugarAcidityZoneResponse> points,
    @NonNull List<@NonNull UnplottableAppleVarietySugarAcidityZonePointResponse> unplottable
) {
}
