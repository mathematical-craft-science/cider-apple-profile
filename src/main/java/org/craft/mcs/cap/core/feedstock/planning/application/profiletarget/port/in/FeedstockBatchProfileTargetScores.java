package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.AnalysisScenario;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.Normalizer;
import org.jspecify.annotations.NonNull;


public record FeedstockBatchProfileTargetScores(
    @NonNull AnalysisScenario analysisScenario,
    @NonNull Normalizer normalizerCode,
    @NonNull List<@NonNull FeedstockBatchProfileTargetScore> scores
) {

  public FeedstockBatchProfileTargetScores {
    requireNonNull(analysisScenario, "profile target analysis scenario cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    scores = List.copyOf(requireNonNull(scores, "feedstock batch profile target scores cannot be null"));
  }
}
