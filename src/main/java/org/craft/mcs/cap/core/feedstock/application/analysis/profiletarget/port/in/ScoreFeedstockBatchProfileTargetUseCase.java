package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in;

import org.jspecify.annotations.NonNull;


public interface ScoreFeedstockBatchProfileTargetUseCase {

  @NonNull
  FeedstockBatchProfileTargetScores score(
      @NonNull FeedstockBatchProfileTargetQuery query
  );
}
