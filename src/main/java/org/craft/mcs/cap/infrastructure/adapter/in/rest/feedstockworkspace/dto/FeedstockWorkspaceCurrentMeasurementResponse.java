package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Latest measurement used as current feedstock batch profile in the workspace.")
public record FeedstockWorkspaceCurrentMeasurementResponse(
    @Schema(
        description = "Current measurement identifier.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID measurementId,
    @Schema(description = "Current measurement name.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String name,
    @Schema(description = "Measurement timestamp.", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull Instant measuredAt,
    @Schema(
        description = "Current chemical profile in canonical service units.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ChemicalProfileResponse chemicalProfile,
    @Schema(
        description = "Optional measurement note.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String note
) {
}
