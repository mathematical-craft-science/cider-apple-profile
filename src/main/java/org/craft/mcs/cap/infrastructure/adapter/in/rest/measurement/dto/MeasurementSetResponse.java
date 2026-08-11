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


/** Transport representation of a logical measurement set and its current immutable revision. */
@Schema(
    description = "Current immutable revision and lifecycle state of one logical measurement set."
)
public record MeasurementSetResponse(
    @Schema(
        description = "Stable identifier shared by every immutable revision of this logical set.",
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
        description = "Audit details present only when lifecycle is RETRACTED.",
        nullable = true
    )
    @Nullable MeasurementSetRetractionResponse retraction,
    @Schema(
        description = "Aggregate version used for optimistic locking.",
        minimum = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long version,
    @Schema(
        description = "Identifier of this immutable revision.",
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
        description = "Business action that created this immutable revision.",
        implementation = MeasurementSetRevisionChangeKind.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementSetRevisionChangeKind changeKind,
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
        description = "Reason for a CORRECTION revision; null for INITIAL and RETRACTION.",
        maxLength = 1000,
        nullable = true
    )
    @Nullable String correctionReason,
    @Schema(
        description = "Reason for full logical-set retraction; present only for a RETRACTION revision.",
        maxLength = 1000,
        nullable = true
    )
    @Nullable String retractionReason,
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
        arraySchema = @Schema(
            description = "Active immutable observations and working results contained in this revision. "
                + "The list can be empty when every characteristic result has been retracted.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementRecordResponse.class)
    )
    @NonNull List<@NonNull MeasurementRecordResponse> records,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Retained tombstones for characteristic results retracted from this revision.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementResultRetractionResponse.class)
    )
    @NonNull List<@NonNull MeasurementResultRetractionResponse> resultRetractions,
    @Schema(
        description = "Time when the logical measurement set was created.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant createdAt,
    @Schema(
        description = "Creation time of the current immutable revision.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant updatedAt
) {
}
