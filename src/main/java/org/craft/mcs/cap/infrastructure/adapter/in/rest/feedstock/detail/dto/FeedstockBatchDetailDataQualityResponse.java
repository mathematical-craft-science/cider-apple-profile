package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityStatus;
import org.jspecify.annotations.NonNull;


/**
 * Transport summary of current-profile data quality for feedstock details.
 */
@Schema(description = "Data-quality summary for the feedstock detail current profile.")
public record FeedstockBatchDetailDataQualityResponse(
    @Schema(
        description = "Overall data-quality status.",
        implementation = FeedstockCurrentProfileDataQualityStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockCurrentProfileDataQualityStatus status,
    @ArraySchema(
        arraySchema = @Schema(description = "Data-quality indicators.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockBatchDetailDataQualityIndicatorResponse.class)
    )
    @NonNull List<@NonNull FeedstockBatchDetailDataQualityIndicatorResponse> indicators
) {
}
