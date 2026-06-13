package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.jspecify.annotations.NonNull;


public interface CreateFeedstockBatchUseCase {

  @NonNull
  FeedstockBatchCreated create(@NonNull CreateFeedstockBatchCommand command);
}
