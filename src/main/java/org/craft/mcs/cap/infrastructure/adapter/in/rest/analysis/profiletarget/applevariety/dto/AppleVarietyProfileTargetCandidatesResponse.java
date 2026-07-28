package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware apple variety candidate search response.")
public record AppleVarietyProfileTargetCandidatesResponse(
    @Schema(
        description = "Analysis scenario.",
        implementation = ProfileTargetAnalysisScenario.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileTargetAnalysisScenario analysisScenario,
    @Schema(
        description = "Normalizer resolved for the analysis scenario.",
        implementation = ProfileAxisNormalizerCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @ArraySchema(
        arraySchema = @Schema(description = "Apple variety candidates.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = AppleVarietyProfileTargetCandidateResponse.class)
    )
    @NonNull List<@NonNull AppleVarietyProfileTargetCandidateResponse> candidates
) {
}
