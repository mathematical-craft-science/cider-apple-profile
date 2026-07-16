package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessStatus;
import org.jspecify.annotations.NonNull;


/**
 * Transport summary of whether the current profile can be used for production planning.
 */
@Schema(description = "Planning-readiness summary for the feedstock detail current profile.")
public record FeedstockBatchDetailPlanningReadinessResponse(
    @Schema(
        description = "Overall planning-readiness status.",
        implementation = FeedstockPlanningReadinessStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningReadinessStatus status,
    @ArraySchema(
        arraySchema = @Schema(description = "Planning-readiness checks.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockBatchDetailPlanningReadinessCheckResponse.class)
    )
    @NonNull List<@NonNull FeedstockBatchDetailPlanningReadinessCheckResponse> checks
) {
}
