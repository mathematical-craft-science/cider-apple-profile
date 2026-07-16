package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningView;
import org.jspecify.annotations.NonNull;


public interface QueryFeedstockPlanningBatchesUseCase {

  @NonNull
  FeedstockPlanningView query(@NonNull FeedstockPlanningBatchViewQuery query);
}
