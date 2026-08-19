package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileDataQualityIndicatorCode;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSignalSeverity;
import org.jspecify.annotations.NonNull;


@Schema(description = "Machine-readable data quality indicator for a feedstock workspace batch.")
public record FeedstockDataQualityIndicatorResponse(
    @Schema(
        description = "Indicator code.",
        implementation = FeedstockCurrentProfileDataQualityIndicatorCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockCurrentProfileDataQualityIndicatorCode code,
    @Schema(
        description = "Semantic severity of the indicator. API clients choose the visual representation.",
        enumAsRef = true,
        implementation = FeedstockCurrentProfileSignalSeverity.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockCurrentProfileSignalSeverity severity,
    @Schema(description = "Human-readable indicator message.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String message
) {
}
