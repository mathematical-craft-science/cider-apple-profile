package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockPlanningReadinessCheckCode;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockPlanningReadinessFactor;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSignalSeverity;
import org.jspecify.annotations.NonNull;


@Schema(description = "Single planning-readiness check for a feedstock workspace batch.")
public record FeedstockPlanningReadinessCheckResponse(
    @Schema(
        description = "Checked planning factor.",
        implementation = FeedstockPlanningReadinessFactor.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningReadinessFactor factor,
    @Schema(
        description = "Semantic severity of the check. API clients choose the visual representation.",
        enumAsRef = true,
        implementation = FeedstockCurrentProfileSignalSeverity.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockCurrentProfileSignalSeverity severity,
    @Schema(
        description = "Stable machine-readable check code.",
        implementation = FeedstockPlanningReadinessCheckCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningReadinessCheckCode code,
    @Schema(description = "Human-readable check message.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String message
) {
}
