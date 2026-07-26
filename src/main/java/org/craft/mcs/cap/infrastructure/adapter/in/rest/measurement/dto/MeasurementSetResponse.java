package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(
    description = "Current immutable revision and lifecycle state of one logical measurement set."
)
public record MeasurementSetResponse(
    @Schema(
        description = "Stable identifier shared by every correction revision of this logical set.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID measurementSetId,
    @Schema(
        description = "Workspace that owns the measured subject and this evidence.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @Schema(
        description = "Business aggregate whose material state was measured.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementSubjectResponse subject,
    @Schema(
        description = "Whether this logical set contributes to current projections or has been retracted.",
        implementation = MeasurementSetLifecycle.class,
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementSetLifecycle lifecycle,
    @Schema(
        description = "Aggregate version used for optimistic locking.",
        minimum = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long version,
    @Schema(
        description = "Identifier of this immutable correction revision.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID revisionId,
    @Schema(
        description = "One-based sequence number of this revision within the logical set.",
        minimum = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    int revisionNumber,
    @Schema(
        description = "Immutable material state to which this measurement revision applies. "
            + "Corrections retain the same state identifier.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID materialStateId,
    @Schema(
        description = "Human-readable name stored in this revision.",
        minLength = 1,
        maxLength = 160,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String name,
    @Schema(
        description = "Optional note stored in this revision.",
        maxLength = 2000,
        nullable = true
    )
    @Nullable String note,
    @Schema(
        description = "Reason for this correction; null only for the initial revision.",
        maxLength = 1000,
        nullable = true
    )
    @Nullable String correctionReason,
    @Schema(
        description = "Time when this immutable revision was created.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant revisionCreatedAt,
    @Schema(
        description = "CAP actor that created this immutable revision.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementActorReferenceResponse actor,
    @ArraySchema(
        minItems = 1,
        arraySchema = @Schema(
            description = "Immutable observations and working results contained in this revision.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementRecordResponse.class)
    )
    @NonNull List<@NonNull MeasurementRecordResponse> records,
    @Schema(
        description = "Time when the logical measurement set was created.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant createdAt,
    @Schema(
        description = "Time when the logical set lifecycle or current revision last changed.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant updatedAt
) {
}
