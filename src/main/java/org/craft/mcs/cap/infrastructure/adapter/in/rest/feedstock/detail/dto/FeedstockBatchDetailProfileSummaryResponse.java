package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;


/**
 * Flat current-profile summary for feedstock detail cards.
 */
@Schema(
    description = """
        Compact current chemical profile projection for feedstock detail cards. This is derived from
        the latest measurement and does not replace latestMeasurement.chemicalProfile.
        """
)
public record FeedstockBatchDetailProfileSummaryResponse(
    @Schema(
        description = "Sugar in canonical SG units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockBatchDetailProfileValueResponse sugar,
    @Schema(
        description = "Titratable acidity in canonical g/L as malic acid units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockBatchDetailProfileValueResponse titratableAcidity,
    @Schema(
        description = "pH value.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockBatchDetailProfileValueResponse ph,
    @Schema(
        description = "Tannin in canonical mg/L TAE units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockBatchDetailProfileValueResponse tannin,
    @Schema(
        description = "Yeast-assimilable nitrogen in canonical mg N/L units.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockBatchDetailProfileValueResponse yan
) {
}
