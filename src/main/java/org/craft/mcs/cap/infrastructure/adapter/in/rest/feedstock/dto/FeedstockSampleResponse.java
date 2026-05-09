package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.jspecify.annotations.NonNull;


public record FeedstockSampleResponse(
    @NonNull String sampleId,
    @NonNull String ciderMakerId,
    @NonNull String batchId,
    @NonNull String name,
    @NonNull FeedstockType type
) {
}
