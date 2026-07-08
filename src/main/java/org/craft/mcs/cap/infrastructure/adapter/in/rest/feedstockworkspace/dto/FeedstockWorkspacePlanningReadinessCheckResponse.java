package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockPlanningReadinessFactor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentProfileSignalSeverity;
import org.jspecify.annotations.NonNull;


@Schema(description = "Single planning-readiness check for a feedstock workspace batch.")
public record FeedstockWorkspacePlanningReadinessCheckResponse(
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
    @Schema(description = "Machine-readable check code.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String code,
    @Schema(description = "Human-readable check message.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String message
) {
}
