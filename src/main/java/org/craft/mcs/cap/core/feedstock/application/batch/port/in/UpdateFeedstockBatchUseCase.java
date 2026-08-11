package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchView;
import org.jspecify.annotations.NonNull;


public interface UpdateFeedstockBatchUseCase {

  @NonNull
  FeedstockBatchView update(@NonNull UpdateFeedstockBatchCommand command);
}
