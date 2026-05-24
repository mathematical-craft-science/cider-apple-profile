package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock batch source contribution in an optimized composition candidate.")
public record FeedstockBatchCompositionOptimizationSourceResponse(
    @Schema(description = "Feedstock batch identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID batchId,
    @Schema(description = "Requested volume in liters, when absolute-volume optimization is used.", example = "25.0", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal requestedVolumeLiters,
    @Schema(description = "Relative source proportion.", example = "0.25", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal proportion
) {
}
