package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityStatus;
import org.jspecify.annotations.NonNull;


@Schema(description = "Data quality summary for a feedstock workspace batch.")
public record FeedstockWorkspaceDataQualityResponse(
    @Schema(
        description = "Overall data quality status.",
        implementation = FeedstockCurrentProfileDataQualityStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockCurrentProfileDataQualityStatus status,
    @ArraySchema(
        arraySchema = @Schema(description = "Data quality indicators.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockWorkspaceDataQualityIndicatorResponse.class)
    )
    @NonNull List<@NonNull FeedstockWorkspaceDataQualityIndicatorResponse> indicators
) {
}
