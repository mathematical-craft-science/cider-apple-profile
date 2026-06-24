package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-analysis metadata for the Feedstock Workspace response.")
public record FeedstockWorkspaceTargetAnalysisResponse(
    @Schema(
        description = "Scoring purpose.",
        implementation = ProfileTargetScoringPurpose.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileTargetScoringPurpose purpose,
    @Schema(
        description = "Normalizer selected for this scoring purpose.",
        implementation = ProfileAxisNormalizerCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileAxisNormalizerCode normalizerCode
) {
}
