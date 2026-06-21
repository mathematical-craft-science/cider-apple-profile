package org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in;

import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


public interface PredictFeedstockBatchCurrentPotentialAbvUseCase {

  @NonNull
  FeedstockBatchCurrentPotentialAbvPrediction predict(@NonNull FeedstockBatchId batchId);
}
