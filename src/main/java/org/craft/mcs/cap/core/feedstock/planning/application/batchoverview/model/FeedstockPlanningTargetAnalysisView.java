package org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.model;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockPlanningTargetAnalysisView(
    @NonNull ProfileTargetAnalysisScenario analysisScenario,
    @NonNull ProfileAxisNormalizerCode normalizerCode
) {

  public FeedstockPlanningTargetAnalysisView {
    requireNonNull(analysisScenario, "profile target analysis scenario cannot be null");
    requireNonNull(normalizerCode, "profile target normalizer code cannot be null");
  }
}
