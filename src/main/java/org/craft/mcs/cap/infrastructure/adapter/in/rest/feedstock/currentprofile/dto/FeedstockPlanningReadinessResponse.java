package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockPlanningReadinessStatus;
import org.jspecify.annotations.NonNull;


@Schema(description = "Readiness summary for using a feedstock batch in composition planning.")
public record FeedstockPlanningReadinessResponse(
    @Schema(
        description = "Overall planning-readiness status.",
        implementation = FeedstockPlanningReadinessStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningReadinessStatus status,
    @ArraySchema(
        arraySchema = @Schema(description = "Planning-readiness checks.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockPlanningReadinessCheckResponse.class)
    )
    @NonNull List<@NonNull FeedstockPlanningReadinessCheckResponse> checks
) {
}
