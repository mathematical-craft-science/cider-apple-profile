package org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.port.in;

import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.contract.FeedstockPlanningView;
import org.jspecify.annotations.NonNull;


public interface QueryFeedstockPlanningBatchesUseCase {

  @NonNull
  FeedstockPlanningView query(@NonNull FeedstockPlanningBatchViewQuery query);
}
