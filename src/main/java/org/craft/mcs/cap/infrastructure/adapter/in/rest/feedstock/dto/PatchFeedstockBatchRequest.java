package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to partially update editable feedstock batch fields.")
public record PatchFeedstockBatchRequest(
    @Schema(description = "Cider maker identifier used for ownership validation.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff")
    @NonNull UUID ciderMakerId,
    @Schema(description = "New batch name, when changed.", example = "October Kingston Black harvest")
    @Nullable String name,
    @Schema(description = "New feedstock type, when changed.", implementation = FeedstockType.class, example = "APPLE_JUICE")
    @Nullable FeedstockType type,
    @Schema(description = "New season date range, when changed.")
    @Nullable FeedstockSeasonRequest season,
    @Schema(description = "New source or origin. Blank value clears the source.", example = "North orchard block A")
    @Nullable String source,
    @Schema(description = "New available volume in liters, when changed.", example = "240.0")
    @Nullable BigDecimal availableVolumeLiters,
    @Schema(description = "New note. Blank value clears the note.", example = "Stored for two weeks before pressing.")
    @Nullable String note
) {
}
