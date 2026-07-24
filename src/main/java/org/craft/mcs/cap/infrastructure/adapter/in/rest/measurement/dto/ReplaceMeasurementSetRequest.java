package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(
    description = "Replaces the current representation by appending a complete immutable correction revision."
)
public record ReplaceMeasurementSetRequest(
    @Schema(
        description = "Optimistic-locking version returned by the latest read of the logical set.",
        minimum = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long expectedVersion,
    @Schema(
        description = "Human-readable name stored in the new revision.",
        minLength = 1,
        maxLength = 160,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String name,
    @Schema(
        description = "Optional note stored in the new revision; null clears the previous note.",
        maxLength = 2000,
        nullable = true
    )
    @Nullable String note,
    @Schema(
        description = "Required explanation of why the previous revision is being corrected.",
        minLength = 1,
        maxLength = 1000,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String correctionReason,
    @ArraySchema(
        minItems = 1,
        arraySchema = @Schema(
            description = "Complete characteristic representation for the new revision.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementCharacteristicRequest.class)
    )
    @NonNull List<@NonNull MeasurementCharacteristicRequest> measurements
) {
}
