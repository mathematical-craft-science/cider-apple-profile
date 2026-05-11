package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


public interface PredictFeedstockBatchCurrentPotentialAbvUseCase {

  @NonNull
  FeedstockBatchCurrentPotentialAbvPrediction predict(@NonNull FeedstockBatchId batchId);
}
