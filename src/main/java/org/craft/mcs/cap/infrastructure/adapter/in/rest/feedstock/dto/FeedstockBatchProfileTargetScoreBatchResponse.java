package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


@Schema(description = "Batch response containing feedstock target distance scores.")
public record FeedstockBatchProfileTargetScoreBatchResponse(
    @Schema(description = "Scoring purpose.", implementation = ProfileTargetScoringPurpose.class)
    @NonNull ProfileTargetScoringPurpose purpose,
    @Schema(description = "Normalizer used.", implementation = ProfileAxisNormalizerCode.class)
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @ArraySchema(schema = @Schema(description = "Feedstock target score.", implementation = FeedstockBatchProfileTargetScoreResponse.class))
    @NonNull List<@NonNull FeedstockBatchProfileTargetScoreResponse> scores
) {
}
