package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to create a feedstock sample inside an existing batch.")
public record CreateFeedstockSampleRequest(
    @Schema(description = "Feedstock batch identifier.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f") @NonNull UUID batchId,
    @Schema(description = "Sample name.", example = "Pressing sample #1") @NonNull String name,
    @Schema(description = "Feedstock type.", implementation = FeedstockType.class, example = "APPLE_JUICE") @NonNull FeedstockType type,
    @Schema(description = "Measured chemical profile.") @NonNull ChemicalProfileRequest chemicalProfile,
    @Schema(description = "Timestamp when the sample was measured.", format = "date-time", example = "2026-10-18T10:15:30Z") @NonNull Instant measuredAt,
    @Schema(description = "Optional sample note.", example = "Measured immediately after pressing.") @Nullable String note
) {
}
