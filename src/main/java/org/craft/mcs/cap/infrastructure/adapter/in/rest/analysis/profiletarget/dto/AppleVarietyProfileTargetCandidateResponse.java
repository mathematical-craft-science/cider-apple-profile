package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetDistanceResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware apple variety candidate for analysis screens.")
public record AppleVarietyProfileTargetCandidateResponse(
    @Schema(description = "Apple variety identifier.", format = "uuid")
    @NonNull UUID varietyId,
    @Schema(description = "Apple variety name.", example = "Kingston Black")
    @NonNull String varietyName,
    @Schema(description = "Candidate profile point.")
    @NonNull SugarAcidityProfilePointResponse point,
    @Schema(description = "Distance to selected target.")
    @NonNull ProfileTargetDistanceResponse distance,
    @Schema(description = "Target-aware contribution role.")
    @NonNull CompositionContributionRoleAssessmentResponse contributionRole
) {
}
