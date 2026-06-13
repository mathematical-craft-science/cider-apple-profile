package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto.CompositionContributionRoleAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware current feedstock batch candidate for analysis screens.")
public record FeedstockBatchProfileTargetCandidateResponse(
    @Schema(description = "Feedstock batch identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID batchId,
    @Schema(description = "Current measurement identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID currentMeasurementId,
    @Schema(description = "Current measurement name.", example = "Pressing measurement #1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String currentMeasurementName,
    @Schema(description = "Candidate current profile point.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityProfilePointResponse point,
    @Schema(description = "Distance to selected target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetDistanceResponse distance,
    @Schema(description = "Target-aware contribution role.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CompositionContributionRoleAssessmentResponse contributionRole
) {
}
