package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


public record FeedstockBatchBlendProfileTargetScoreBatchResponse(
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull List<@NonNull FeedstockBatchBlendProfileTargetScoreResponse> scores
) {
}
