package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.AnalysisScenario;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.Normalizer;
import org.jspecify.annotations.NonNull;


public record FeedstockBatchProfileTargetCandidates(
    @NonNull AnalysisScenario analysisScenario,
    @NonNull Normalizer normalizerCode,
    @NonNull List<@NonNull FeedstockBatchProfileTargetCandidate> candidates
) {

  public FeedstockBatchProfileTargetCandidates {
    requireNonNull(analysisScenario, "profile target analysis scenario cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    candidates = List.copyOf(requireNonNull(candidates, "feedstock batch profile target candidates cannot be null"));
  }
}
