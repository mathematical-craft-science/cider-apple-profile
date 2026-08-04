package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;


/**
 * Compact current-characteristics projection for feedstock planning board rows.
 *
 * <p>This response is optimized for the operational board table. It gives the frontend stable,
 * flat, unit-aware values without forcing it to interpret the full measurement tree. Values are
 * independently selected from current evidence and may belong to different measurement sets.
 */
@Schema(
    description = """
        Compact composite current-profile projection for feedstock planning board rows. Each
        characteristic is independently selected from applicable current evidence and may come from
        a different MeasurementSet. Use profileEvidence to identify the selected sources. This is a
        read model, not an edit model.
        """
)
public record FeedstockCurrentProfileSummaryResponse(
    @Schema(
        description = "Specific Gravity in canonical SG units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfileValueResponse specificGravity,
    @Schema(
        description = "Titratable acidity in canonical g/L as malic acid units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfileValueResponse titratableAcidity,
    @Schema(
        description = "pH value.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfileValueResponse ph,
    @Schema(
        description = "Tannin in canonical mg/L TAE units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfileValueResponse tannin,
    @Schema(
        description = "Yeast-assimilable nitrogen in canonical mg N/L units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfileValueResponse yan
) {
}
