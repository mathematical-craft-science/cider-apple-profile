package org.craft.mcs.cap.core.feedstock.application.analysis.readiness.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


public interface AssessFeedstockBatchCurrentFermentationStartReadinessUseCase {

  @NonNull
  FeedstockBatchCurrentFermentationStartReadinessResult assess(@NonNull FeedstockBatchId batchId);
}
