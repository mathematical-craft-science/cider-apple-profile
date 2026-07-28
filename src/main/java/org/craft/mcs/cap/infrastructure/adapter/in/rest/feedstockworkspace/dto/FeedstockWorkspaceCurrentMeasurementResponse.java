package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(
    description = """
        Compatibility-shaped current-profile summary. Measurement metadata identifies the latest
        contributing MeasurementSet, while chemicalProfile is composite and may contain evidence
        selected from older MeasurementSets for other characteristics.
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
        description = """
            Composite current profile in canonical service units. Characteristics can originate
            from different MeasurementSets; use profileEvidence for per-characteristic provenance.
            """,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ChemicalProfileResponse chemicalProfile,
    @Schema(
        description = "Optional note of the latest contributing MeasurementSet.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String note
) {
}
