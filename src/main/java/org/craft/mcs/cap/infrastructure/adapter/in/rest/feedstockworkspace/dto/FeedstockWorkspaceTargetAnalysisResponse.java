package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-analysis metadata for the Feedstock Workspace response.")
public record FeedstockWorkspaceTargetAnalysisResponse(
    @Schema(
        description = "Analysis scenario.",
        implementation = ProfileTargetAnalysisScenario.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileTargetAnalysisScenario analysisScenario,
    @Schema(
        description = "Normalizer resolved for this analysis scenario.",
        implementation = ProfileAxisNormalizerCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileAxisNormalizerCode normalizerCode
) {
}
