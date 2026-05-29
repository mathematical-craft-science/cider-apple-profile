package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to replace editable feedstock batch fields.")
public record UpdateFeedstockBatchRequest(
    @Schema(description = "Cider maker identifier used for ownership validation.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID ciderMakerId,
    @Schema(description = "Batch name.", example = "October Kingston Black harvest", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String name,
    @Schema(description = "Feedstock type.", implementation = FeedstockType.class, example = "APPLE_JUICE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockType type,
    @Schema(description = "Season date range.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockSeasonRequest season,
    @Schema(description = "Optional source or origin. Blank value clears the source.", example = "North orchard block A", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String source,
    @Schema(description = "Available volume in liters.", example = "240.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BigDecimal availableVolumeLiters,
    @Schema(description = "Optional note. Blank value clears the note.", example = "Stored for two weeks before pressing.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String note
) {
}
