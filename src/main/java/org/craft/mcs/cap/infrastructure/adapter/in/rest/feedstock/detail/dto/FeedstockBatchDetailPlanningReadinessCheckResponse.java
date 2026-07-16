package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessCheckCode;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessFactor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSignalSeverity;
import org.jspecify.annotations.NonNull;


/**
 * Transport representation of one production-planning readiness check.
 */
@Schema(description = "One planning-readiness check for the feedstock detail current profile.")
public record FeedstockBatchDetailPlanningReadinessCheckResponse(
    @Schema(
        description = "Checked planning factor.",
        implementation = FeedstockPlanningReadinessFactor.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningReadinessFactor factor,
    @Schema(
        description = "Semantic severity of the check.",
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
    @Schema(description = "Short check explanation.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String message
) {
}
