package org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.jspecify.annotations.NonNull;


public interface ListFeedstockBatchesUseCase {

  @NonNull
  List<@NonNull FeedstockBatchView> list(@NonNull ListFeedstockBatchesQuery query);
}
