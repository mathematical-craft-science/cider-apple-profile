package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityIndicatorCode;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSignalSeverity;
import org.jspecify.annotations.NonNull;


/**
 * Transport representation of one data-quality signal for the current profile.
 */
@Schema(description = "One data-quality signal for the feedstock detail current profile.")
public record FeedstockBatchDetailDataQualityIndicatorResponse(
    @Schema(
        description = "Stable data-quality indicator code.",
        implementation = FeedstockCurrentProfileDataQualityIndicatorCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockCurrentProfileDataQualityIndicatorCode code,
    @Schema(
        description = "Semantic severity of the indicator.",
        implementation = FeedstockCurrentProfileSignalSeverity.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockCurrentProfileSignalSeverity severity,
    @Schema(description = "Short data-quality explanation.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String message
) {
}
