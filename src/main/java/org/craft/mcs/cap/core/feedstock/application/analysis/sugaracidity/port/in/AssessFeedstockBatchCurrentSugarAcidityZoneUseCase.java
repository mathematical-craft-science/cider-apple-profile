package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


public interface AssessFeedstockBatchCurrentSugarAcidityZoneUseCase {

  @NonNull
  FeedstockBatchCurrentSugarAcidityZoneResult assess(@NonNull FeedstockBatchId batchId);
}
