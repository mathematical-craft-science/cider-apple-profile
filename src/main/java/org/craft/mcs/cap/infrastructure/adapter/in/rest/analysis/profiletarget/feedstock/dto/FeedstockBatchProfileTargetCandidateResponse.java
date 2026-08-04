package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionRoleAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetScoringCharacteristicsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockProfileEvidenceResponse;
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
    @ArraySchema(
        arraySchema = @Schema(
            description = "Per-characteristic evidence used for target scoring and role assessment.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = FeedstockProfileEvidenceResponse.class)
    )
    @NonNull List<@NonNull FeedstockProfileEvidenceResponse> profileEvidence,
    @Schema(description = "Candidate current scoring characteristics.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetScoringCharacteristicsResponse characteristics,
    @Schema(description = "Distance to selected target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetDistanceResponse distance,
    @Schema(description = "Target-aware contribution role.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CompositionContributionRoleAssessmentResponse contributionRole
) {
}
