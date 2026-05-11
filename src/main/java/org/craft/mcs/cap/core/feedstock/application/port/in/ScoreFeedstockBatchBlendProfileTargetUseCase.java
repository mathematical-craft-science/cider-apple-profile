package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.jspecify.annotations.NonNull;


public interface ScoreFeedstockBatchBlendProfileTargetUseCase {

  @NonNull
  FeedstockBatchBlendProfileTargetScores score(
      @NonNull ScoreFeedstockBatchBlendProfileTargetCommand command
  );
}
