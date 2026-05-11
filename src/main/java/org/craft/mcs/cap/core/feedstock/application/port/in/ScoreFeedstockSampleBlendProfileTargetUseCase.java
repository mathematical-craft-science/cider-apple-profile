package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.jspecify.annotations.NonNull;


public interface ScoreFeedstockSampleBlendProfileTargetUseCase {

  @NonNull
  FeedstockSampleBlendProfileTargetScores score(
      @NonNull ScoreFeedstockSampleBlendProfileTargetCommand command
  );
}
