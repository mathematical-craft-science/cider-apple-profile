package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.List;
import org.jspecify.annotations.NonNull;


public record FeedstockSampleListResponse(
    @NonNull List<@NonNull FeedstockSampleResponse> samples
) {
}
