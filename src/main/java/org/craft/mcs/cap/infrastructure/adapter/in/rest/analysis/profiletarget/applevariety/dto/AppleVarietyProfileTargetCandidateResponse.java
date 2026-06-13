package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionRoleAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware apple variety candidate for analysis screens.")
public record AppleVarietyProfileTargetCandidateResponse(
    @Schema(description = "Apple variety identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID varietyId,
    @Schema(description = "Apple variety name.", example = "Kingston Black", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String varietyName,
    @Schema(description = "Candidate profile point.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityProfilePointResponse point,
    @Schema(description = "Distance to selected target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetDistanceResponse distance,
    @Schema(description = "Target-aware contribution role.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CompositionContributionRoleAssessmentResponse contributionRole
) {
}
