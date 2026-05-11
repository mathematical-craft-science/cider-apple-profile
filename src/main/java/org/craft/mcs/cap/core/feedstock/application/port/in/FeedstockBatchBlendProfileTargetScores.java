package org.craft.mcs.cap.core.feedstock.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchBlendProfileTargetScores(
    @NonNull BlendProfileScoringPurpose purpose,
    @NonNull BlendProfileAxisNormalizerCode normalizerCode,
    @NonNull List<@NonNull FeedstockBatchBlendProfileTargetScore> scores
) {

  public FeedstockBatchBlendProfileTargetScores {
    requireNonNull(purpose, "blend profile scoring purpose cannot be null");
    requireNonNull(normalizerCode, "blend profile axis normalizer code cannot be null");
    scores = List.copyOf(requireNonNull(scores, "feedstock batch blend profile target scores cannot be null"));
  }
}
