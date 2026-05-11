package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


public record FeedstockBatchBlendProfileTargetScoreBatchResponse(
    @NonNull BlendProfileScoringPurpose purpose,
    @NonNull BlendProfileAxisNormalizerCode normalizerCode,
    @NonNull List<@NonNull FeedstockBatchBlendProfileTargetScoreResponse> scores
) {
}
