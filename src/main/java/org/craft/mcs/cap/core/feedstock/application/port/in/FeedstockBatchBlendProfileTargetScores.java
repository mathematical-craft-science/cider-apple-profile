package org.craft.mcs.cap.core.feedstock.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchBlendProfileTargetScores(
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull List<@NonNull FeedstockBatchBlendProfileTargetScore> scores
) {

  public FeedstockBatchBlendProfileTargetScores {
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    scores = List.copyOf(requireNonNull(scores, "feedstock batch blend profile target scores cannot be null"));
  }
}
