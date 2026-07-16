package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningRoleEvidenceDirection;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningRoleEvidenceEffect;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningRoleEvidenceSeverity;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.jspecify.annotations.NonNull;


@Schema(description = "Typed semantic fact for a feedstock workspace target candidate role explanation.")
public record FeedstockWorkspaceRoleEvidenceFactResponse(
    @Schema(
        description = "Evaluated chemical profile axis.",
        implementation = ProfileEvaluationAxis.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileEvaluationAxis axis,
    @Schema(
        description = "Direction of the source value relative to the target coordinate.",
        implementation = FeedstockPlanningRoleEvidenceDirection.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningRoleEvidenceDirection direction,
    @Schema(
        description = "Typed effect of this source axis in the selected target context.",
        implementation = FeedstockPlanningRoleEvidenceEffect.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningRoleEvidenceEffect effect,
    @Schema(
        description = "Strength of this evidence fact for planning interpretation.",
        implementation = FeedstockPlanningRoleEvidenceSeverity.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningRoleEvidenceSeverity severity
) {
}
