package org.craft.mcs.cap.core.feedstock.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


public interface ListFeedstockSamplesUseCase {

  @NonNull
  List<@NonNull FeedstockSampleSummary> listByBatchId(@NonNull FeedstockBatchId batchId);
}
