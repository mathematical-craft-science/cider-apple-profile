package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.List;
import org.jspecify.annotations.NonNull;


public record FeedstockSampleSugarAcidityZonePointBatchResponse(
    @NonNull List<@NonNull FeedstockSampleSugarAcidityZoneResponse> points,
    @NonNull List<@NonNull UnplottableFeedstockSampleSugarAcidityZonePointResponse> unplottable
) {
}
