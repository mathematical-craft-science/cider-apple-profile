package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in;

import org.jspecify.annotations.NonNull;


public interface ScoreFeedstockBatchProfileTargetUseCase {

  @NonNull
  FeedstockBatchProfileTargetScores score(
      @NonNull FeedstockBatchProfileTargetQuery query
  );
}
