package org.craft.mcs.cap.core.feedstock.batch.application.analysis.abv.port.in;

import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


public interface PredictFeedstockBatchCurrentPotentialAbvUseCase {

  @NonNull
  FeedstockBatchCurrentPotentialAbvPrediction predict(@NonNull FeedstockBatchId batchId);
}
