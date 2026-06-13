package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionRoleAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware candidate information for one feedstock batch.")
public record FeedstockWorkspaceTargetCandidateResponse(
    @Schema(description = "Current profile point used for target scoring.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityProfilePointResponse point,
    @Schema(description = "Distance from current profile to selected target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetDistanceResponse distance,
    @Schema(description = "Target-aware contribution role.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CompositionContributionRoleAssessmentResponse contributionRole
) {
}
