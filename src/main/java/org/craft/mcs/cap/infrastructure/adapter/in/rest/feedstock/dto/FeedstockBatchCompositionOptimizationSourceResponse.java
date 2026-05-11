package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.math.BigDecimal;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record FeedstockBatchCompositionOptimizationSourceResponse(
    @NonNull UUID batchId,
    @Nullable BigDecimal requestedVolumeLiters,
    @Nullable BigDecimal proportion
) {
}
