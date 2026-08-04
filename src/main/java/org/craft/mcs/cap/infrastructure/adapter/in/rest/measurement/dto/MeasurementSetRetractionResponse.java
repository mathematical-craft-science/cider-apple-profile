package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.jspecify.annotations.NonNull;


/** Audit details retained when a complete logical measurement set is retracted. */
@Schema(description = "Actor, time, and reason retained for a complete measurement-set retraction.")
public record MeasurementSetRetractionResponse(
    @Schema(
        description = "CAP actor that retracted the complete measurement set.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementActorReferenceResponse actor,
    @Schema(
        description = "Time when the complete measurement set was retracted.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant retractedAt,
    @Schema(
        description = "Reason supplied for retracting the complete measurement set.",
        minLength = 1,
        maxLength = 1000,
        example = "Laboratory invalidated the complete report",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String reason
) {
}
