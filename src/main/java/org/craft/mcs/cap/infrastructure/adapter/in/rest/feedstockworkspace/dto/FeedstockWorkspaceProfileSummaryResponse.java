package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;


/**
 * Compact current chemical profile projection for feedstock planning board rows.
 *
 * <p>This response is optimized for the operational board table. It gives the frontend stable,
 * flat, unit-aware values without forcing it to interpret the full measurement tree. It must not be
 * used as an edit model or as a replacement for the original current measurement payload.
 */
@Schema(
    description = """
        Compact current chemical profile projection for feedstock planning board rows. This is a
        board read model, not an edit model and not a replacement for currentMeasurement. It gives
        stable, flat, unit-aware values derived from the current measurement.
        """
)
public record FeedstockWorkspaceProfileSummaryResponse(
    @Schema(
        description = "Sugar in canonical SG units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockWorkspaceProfileValueResponse sugar,
    @Schema(
        description = "Titratable acidity in canonical g/L as malic acid units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockWorkspaceProfileValueResponse titratableAcidity,
    @Schema(
        description = "pH value.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockWorkspaceProfileValueResponse ph,
    @Schema(
        description = "Tannin in canonical mg/L TAE units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockWorkspaceProfileValueResponse tannin,
    @Schema(
        description = "Yeast-assimilable nitrogen in canonical mg N/L units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockWorkspaceProfileValueResponse yan
) {
}
