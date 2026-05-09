package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.math.BigDecimal;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record CreateFeedstockBatchRequest(
    @NonNull UUID ciderMakerId,
    @NonNull String name,
    @NonNull FeedstockSeasonRequest season,
    @Nullable String source,
    @NonNull BigDecimal availableVolumeLiters,
    @Nullable String note
) {
}
