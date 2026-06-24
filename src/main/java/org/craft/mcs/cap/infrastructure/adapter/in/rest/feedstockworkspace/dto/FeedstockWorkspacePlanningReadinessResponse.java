package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningReadinessStatus;
import org.jspecify.annotations.NonNull;


@Schema(description = "Readiness summary for using a feedstock batch in composition planning.")
public record FeedstockWorkspacePlanningReadinessResponse(
    @Schema(
        description = "Overall planning-readiness status.",
        implementation = FeedstockPlanningReadinessStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningReadinessStatus status,
    @ArraySchema(
        arraySchema = @Schema(description = "Planning-readiness checks.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockWorkspacePlanningReadinessCheckResponse.class)
    )
    @NonNull List<@NonNull FeedstockWorkspacePlanningReadinessCheckResponse> checks
) {
}
