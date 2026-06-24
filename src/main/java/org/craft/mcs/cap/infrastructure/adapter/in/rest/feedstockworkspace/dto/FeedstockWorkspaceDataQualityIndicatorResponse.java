package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningDataQualityIndicatorCode;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningSignalSeverity;
import org.jspecify.annotations.NonNull;


@Schema(description = "Machine-readable data quality indicator for a feedstock workspace batch.")
public record FeedstockWorkspaceDataQualityIndicatorResponse(
    @Schema(
        description = "Indicator code.",
        implementation = FeedstockPlanningDataQualityIndicatorCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningDataQualityIndicatorCode code,
    @Schema(
        description = "Semantic severity of the indicator. API clients choose the visual representation.",
        enumAsRef = true,
        implementation = FeedstockPlanningSignalSeverity.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningSignalSeverity severity,
    @Schema(description = "Human-readable indicator message.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String message
) {
}
