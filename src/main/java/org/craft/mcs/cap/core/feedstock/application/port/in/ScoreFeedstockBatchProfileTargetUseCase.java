package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.jspecify.annotations.NonNull;


public interface ScoreFeedstockBatchProfileTargetUseCase {

  @NonNull
  FeedstockBatchProfileTargetScores score(
      @NonNull ScoreFeedstockBatchProfileTargetCommand command
  );
}
