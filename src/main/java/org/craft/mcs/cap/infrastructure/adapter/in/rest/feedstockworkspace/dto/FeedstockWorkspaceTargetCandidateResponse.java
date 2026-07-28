package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionRoleAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetScoringCharacteristicsResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware candidate information for one feedstock batch.")
public record FeedstockWorkspaceTargetCandidateResponse(
    @Schema(
        description = "Material state whose current characteristics were assessed.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMaterialStateId,
    @Schema(
        description = "Current characteristics used for target scoring.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileTargetScoringCharacteristicsResponse characteristics,
    @Schema(
        description = "Distance from current profile to selected target.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileTargetDistanceResponse distance,
    @Schema(description = "Target-aware contribution role.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CompositionContributionRoleAssessmentResponse contributionRole
) {
}
