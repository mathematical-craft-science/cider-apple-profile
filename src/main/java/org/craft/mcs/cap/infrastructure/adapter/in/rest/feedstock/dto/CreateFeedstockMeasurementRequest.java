package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to create a feedstock measurement inside an existing batch.")
public record CreateFeedstockMeasurementRequest(
    @Schema(description = "Measurement name.", example = "Pressing measurement #1", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String name,
    @Schema(description = "Measured chemical profile.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull ChemicalProfileRequest chemicalProfile,
    @Schema(description = "Timestamp when the measurement was taken.", format = "date-time", example = "2026-10-18T10:15:30Z", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull Instant measuredAt,
    @Schema(description = "Optional measurement note.", example = "Measured immediately after pressing.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable String note
) {
}
