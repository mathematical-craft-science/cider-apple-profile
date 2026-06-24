package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockPlanningTargetAnalysisView(
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull ProfileAxisNormalizerCode normalizerCode
) {

  public FeedstockPlanningTargetAnalysisView {
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(normalizerCode, "profile target normalizer code cannot be null");
  }
}
