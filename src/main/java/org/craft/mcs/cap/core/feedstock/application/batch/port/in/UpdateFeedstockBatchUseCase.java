package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.jspecify.annotations.NonNull;


public interface UpdateFeedstockBatchUseCase {

  @NonNull
  FeedstockBatchSummary update(@NonNull UpdateFeedstockBatchCommand command);
}
