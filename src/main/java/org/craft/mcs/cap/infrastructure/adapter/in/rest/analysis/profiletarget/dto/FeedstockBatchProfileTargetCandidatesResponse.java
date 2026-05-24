package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware feedstock batch candidate search response.")
public record FeedstockBatchProfileTargetCandidatesResponse(
    @Schema(description = "Scoring purpose.", implementation = ProfileTargetScoringPurpose.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetScoringPurpose purpose,
    @Schema(description = "Normalizer selected for this scoring purpose.", implementation = ProfileAxisNormalizerCode.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @ArraySchema(
        arraySchema = @Schema(description = "Feedstock batch candidates.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockBatchProfileTargetCandidateResponse.class)
    )
    @NonNull List<@NonNull FeedstockBatchProfileTargetCandidateResponse> candidates
) {
}
