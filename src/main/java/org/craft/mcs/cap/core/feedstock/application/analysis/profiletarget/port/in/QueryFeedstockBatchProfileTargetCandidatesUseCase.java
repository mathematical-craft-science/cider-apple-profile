package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in;

import org.jspecify.annotations.NonNull;


public interface QueryFeedstockBatchProfileTargetCandidatesUseCase {

  @NonNull
  FeedstockBatchProfileTargetCandidates query(
      @NonNull FeedstockBatchProfileTargetCandidateQuery query
  );
}
