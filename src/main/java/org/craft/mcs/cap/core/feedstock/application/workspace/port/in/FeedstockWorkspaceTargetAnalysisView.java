package org.craft.mcs.cap.core.feedstock.application.workspace.port.in;

import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockWorkspaceTargetAnalysisView(
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull ProfileAxisNormalizerCode normalizerCode
) {

  public FeedstockWorkspaceTargetAnalysisView {
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(normalizerCode, "profile target normalizer code cannot be null");
  }
}
