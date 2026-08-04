package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Immutable historical revision of one logical measurement set. */
@Schema(description = "One immutable correction revision retained for measurement audit history.")
public record MeasurementSetRevisionResponse(
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID revisionId,
    @Schema(minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    int revisionNumber,
    @Schema(
        description = "Immutable material state observed by this revision.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID materialStateId,
    @Schema(minLength = 1, maxLength = 160, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String name,
    @Schema(maxLength = 2000, nullable = true)
    @Nullable String note,
    @Schema(
        description = "Reason for this correction; absent only on the initial revision.",
        maxLength = 1000,
        nullable = true
    )
    @Nullable String correctionReason,
    @Schema(format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull Instant createdAt,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull MeasurementActorReferenceResponse actor,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Active evidence represented by this historical revision.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementRecordResponse.class)
    )
    @NonNull List<@NonNull MeasurementRecordResponse> records,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Result retractions represented by this historical revision.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementResultRetractionResponse.class)
    )
    @NonNull List<@NonNull MeasurementResultRetractionResponse> resultRetractions
) {
}
