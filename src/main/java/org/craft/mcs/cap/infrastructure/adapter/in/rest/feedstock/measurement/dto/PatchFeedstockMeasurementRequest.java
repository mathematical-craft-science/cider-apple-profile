package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to partially update a feedstock measurement.")
public record PatchFeedstockMeasurementRequest(
    @Schema(
        description = "New measurement name, when changed.",
        example = "Pressing measurement #1",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String name,
    @Schema(
        description = "Chemical profile axes to merge into the existing profile.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockMeasurementChemicalProfileRequest chemicalProfile,
    @Schema(
        description = "New timestamp when the measurement was taken.",
        format = "date-time",
        example = "2026-10-18T10:15:30Z",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Instant measuredAt,
    @Schema(
        description = "New note. Blank value clears the note.",
        example = "Measured immediately after pressing.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String note
) {
}
