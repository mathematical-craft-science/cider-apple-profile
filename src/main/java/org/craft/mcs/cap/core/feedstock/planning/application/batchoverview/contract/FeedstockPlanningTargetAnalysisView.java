package org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.contract;

import static java.util.Objects.requireNonNull;

import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.AnalysisScenario;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.Normalizer;
import org.jspecify.annotations.NonNull;


public record FeedstockPlanningTargetAnalysisView(
    @NonNull AnalysisScenario analysisScenario,
    @NonNull Normalizer normalizerCode
) {

  public FeedstockPlanningTargetAnalysisView {
    requireNonNull(analysisScenario, "profile target analysis scenario cannot be null");
    requireNonNull(normalizerCode, "profile target normalizer code cannot be null");
  }
}
