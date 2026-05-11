package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock batch response.")
public record FeedstockBatchResponse(
    @Schema(description = "Feedstock batch identifier.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f") @NonNull UUID batchId,
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff") @NonNull UUID ciderMakerId,
    @Schema(description = "Batch name.", example = "October Kingston Black harvest") @NonNull String name,
    @Schema(description = "Feedstock type.", implementation = FeedstockType.class, example = "APPLE_JUICE") @NonNull FeedstockType type,
    @Schema(description = "Harvest season date range.", implementation = FeedstockSeasonResponse.class) @NonNull FeedstockSeasonResponse season,
    @Schema(description = "Optional feedstock source or origin.", example = "North orchard block A") @Nullable String source,
    @Schema(description = "Available feedstock volume in liters.", example = "240.0") @NonNull BigDecimal availableVolumeLiters,
    @Schema(description = "Optional batch note.", example = "Stored for two weeks before pressing.") @Nullable String note
) {
}
