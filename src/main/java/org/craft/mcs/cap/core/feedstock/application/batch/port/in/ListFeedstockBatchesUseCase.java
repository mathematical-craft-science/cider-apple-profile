package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.jspecify.annotations.NonNull;


public interface ListFeedstockBatchesUseCase {

  @NonNull
  List<@NonNull FeedstockBatchSummary> list(@NonNull ListFeedstockBatchesQuery query);
}
