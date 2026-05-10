package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock sample response.")
public record FeedstockSampleResponse(
    @Schema(description = "Feedstock sample identifier.", format = "uuid", example = "ed7c0818-aad9-47dd-97c0-d1d37561ca18") @NonNull UUID sampleId,
    @Schema(description = "Cider maker identifier derived from the batch.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff") @NonNull UUID ciderMakerId,
    @Schema(description = "Feedstock batch identifier.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f") @NonNull UUID batchId,
    @Schema(description = "Sample name.", example = "Pressing sample #1") @NonNull String name,
    @Schema(description = "Feedstock type.", implementation = FeedstockType.class, example = "APPLE_JUICE") @NonNull FeedstockType type,
    @Schema(description = "Timestamp when the sample was measured.", format = "date-time", example = "2026-10-18T10:15:30Z") @Nullable Instant measuredAt,
    @Schema(description = "Optional sample note.", example = "Measured immediately after pressing.") @Nullable String note
) {
}
