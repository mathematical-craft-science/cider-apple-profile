package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;


@Schema(description = "Compact current chemical profile projection for feedstock planning board rows.")
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
