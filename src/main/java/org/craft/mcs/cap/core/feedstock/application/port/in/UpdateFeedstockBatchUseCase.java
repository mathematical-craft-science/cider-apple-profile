package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.jspecify.annotations.NonNull;


public interface UpdateFeedstockBatchUseCase {

  @NonNull
  FeedstockBatchSummary update(@NonNull UpdateFeedstockBatchCommand command);
}
