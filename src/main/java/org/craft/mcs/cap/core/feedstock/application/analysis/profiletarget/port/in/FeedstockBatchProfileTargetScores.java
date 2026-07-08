package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchProfileTargetScores(
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull List<@NonNull FeedstockBatchProfileTargetScore> scores
) {

  public FeedstockBatchProfileTargetScores {
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    scores = List.copyOf(requireNonNull(scores, "feedstock batch profile target scores cannot be null"));
  }
}
