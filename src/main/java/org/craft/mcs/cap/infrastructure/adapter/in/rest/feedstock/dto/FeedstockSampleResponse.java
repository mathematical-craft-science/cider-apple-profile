package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record FeedstockSampleResponse(
    @NonNull String sampleId,
    @NonNull String ciderMakerId,
    @NonNull String batchId,
    @NonNull String name,
    @NonNull FeedstockType type,
    @Nullable Instant measuredAt,
    @Nullable String note
) {
}
