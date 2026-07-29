package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(
    description = """
        Metadata of the latest MeasurementSet contributing evidence to the composite current profile.
        Characteristic values and their independent evidence are exposed by the enclosing batch response.
        """
)
public record FeedstockWorkspaceCurrentMeasurementResponse(
    @Schema(
        description = "Identifier of the latest MeasurementSet contributing to the current profile.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID measurementId,
    @Schema(
        description = "Name of the latest contributing MeasurementSet.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String name,
    @Schema(
        description = "Timestamp of the latest contributing MeasurementSet.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant measuredAt,
    @Schema(
        description = "Optional note of the latest contributing MeasurementSet.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String note
) {
}
