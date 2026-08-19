package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in;

import org.jspecify.annotations.NonNull;


public interface QueryFeedstockBatchProfileTargetCandidatesUseCase {

  @NonNull
  FeedstockBatchProfileTargetCandidates query(
      @NonNull FeedstockBatchProfileTargetQuery query
  );
}
