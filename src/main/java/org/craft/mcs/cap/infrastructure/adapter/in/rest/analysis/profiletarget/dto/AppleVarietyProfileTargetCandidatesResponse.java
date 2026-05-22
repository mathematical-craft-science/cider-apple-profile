package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware apple variety candidate search response.")
public record AppleVarietyProfileTargetCandidatesResponse(
    @Schema(description = "Scoring purpose.", implementation = ProfileTargetScoringPurpose.class)
    @NonNull ProfileTargetScoringPurpose purpose,
    @Schema(description = "Normalizer selected for this scoring purpose.", implementation = ProfileAxisNormalizerCode.class)
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @ArraySchema(schema = @Schema(description = "Apple variety candidates.", implementation = AppleVarietyProfileTargetCandidateResponse.class))
    @NonNull List<@NonNull AppleVarietyProfileTargetCandidateResponse> candidates
) {
}
