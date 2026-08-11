package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionChangeKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Immutable historical revision of one logical measurement set. */
@Schema(description = "One immutable revision retained in the measurement-set audit history.")
public record MeasurementSetRevisionResponse(
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID revisionId,
    @Schema(minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    int revisionNumber,
    @Schema(implementation = MeasurementSetLifecycle.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull MeasurementSetLifecycle lifecycle,
    @Schema(
        implementation = MeasurementSetRevisionChangeKind.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementSetRevisionChangeKind changeKind,
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
        description = "Reason for a CORRECTION revision; absent for INITIAL and RETRACTION.",
        maxLength = 1000,
        nullable = true
    )
    @Nullable String correctionReason,
    @Schema(
        description = "Reason for complete logical-set retraction; present only on a RETRACTION revision.",
        maxLength = 1000,
        nullable = true
    )
    @Nullable String retractionReason,
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
