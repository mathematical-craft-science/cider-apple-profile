package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import org.jspecify.annotations.NonNull;


public interface QueryFeedstockPlanningBatchesUseCase {

  @NonNull
  FeedstockPlanningView query(@NonNull FeedstockPlanningBatchViewQuery query);
}
