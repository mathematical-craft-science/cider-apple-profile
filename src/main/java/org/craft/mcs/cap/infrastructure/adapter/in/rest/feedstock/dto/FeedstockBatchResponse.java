package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record FeedstockBatchResponse(
    @NonNull String batchId,
    @NonNull String ciderMakerId,
    @NonNull String name,
    @NonNull FeedstockSeasonResponse season,
    @Nullable String source,
    @NonNull BigDecimal availableVolumeLiters,
    @Nullable String note
) {
}
