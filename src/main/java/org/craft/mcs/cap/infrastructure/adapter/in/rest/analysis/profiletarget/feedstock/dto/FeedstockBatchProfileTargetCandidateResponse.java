package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionRoleAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetScoringCharacteristicsResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware current feedstock batch candidate for analysis screens.")
public record FeedstockBatchProfileTargetCandidateResponse(
    @Schema(description = "Feedstock batch identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID batchId,
    @Schema(
        description = "Material state whose current characteristics were assessed.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMaterialStateId,
    @Schema(
        description = "Latest measurement set contributing to the composite current profile.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMeasurementId,
    @Schema(
        description = "Name of the latest measurement set contributing to the composite current profile.",
        example = "Pressing measurement #1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String currentMeasurementName,
    @Schema(description = "Candidate current scoring characteristics.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetScoringCharacteristicsResponse characteristics,
    @Schema(description = "Distance to selected target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetDistanceResponse distance,
    @Schema(description = "Target-aware contribution role.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CompositionContributionRoleAssessmentResponse contributionRole
) {
}
