package org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in;

import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.jspecify.annotations.NonNull;


public interface UpdateFeedstockBatchUseCase {

  @NonNull
  FeedstockBatchView update(@NonNull UpdateFeedstockBatchCommand command);
}
