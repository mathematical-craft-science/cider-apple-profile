package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Audit history of every immutable revision belonging to one logical measurement set. */
@Schema(description = "Logical measurement-set lifecycle and immutable correction history.")
public record MeasurementSetRevisionHistoryResponse(
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID measurementSetId,
    @Schema(implementation = MeasurementSetLifecycle.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull MeasurementSetLifecycle lifecycle,
    @Schema(
        description = "Audit details present only when the complete logical set is retracted.",
        nullable = true
    )
    @Nullable MeasurementSetRetractionResponse retraction,
    @Schema(
        description = "Current aggregate version used for optimistic locking.",
        minimum = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long version,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Immutable revisions ordered from newest to oldest.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementSetRevisionResponse.class)
    )
    @NonNull List<@NonNull MeasurementSetRevisionResponse> revisions
) {
}
