package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock measurement response.")
public record FeedstockMeasurementResponse(
    @Schema(description = "Feedstock measurement identifier.", format = "uuid", example = "ed7c0818-aad9-47dd-97c0-d1d37561ca18") @NonNull UUID measurementId,
    @Schema(description = "Cider maker identifier derived from the batch.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff") @NonNull UUID ciderMakerId,
    @Schema(description = "Feedstock batch identifier.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f") @NonNull UUID batchId,
    @Schema(description = "Measurement name.", example = "Pressing measurement #1") @NonNull String name,
    @Schema(description = "Timestamp when the measurement was taken.", format = "date-time", example = "2026-10-18T10:15:30Z") @Nullable Instant measuredAt,
    @Schema(description = "Chemical profile in canonical service units.") @Nullable ChemicalProfileResponse chemicalProfile,
    @Schema(description = "Optional measurement note.", example = "Measured immediately after pressing.") @Nullable String note
) {
}
