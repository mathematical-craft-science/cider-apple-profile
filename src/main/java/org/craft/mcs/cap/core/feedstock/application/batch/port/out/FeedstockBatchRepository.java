package org.craft.mcs.cap.core.feedstock.application.batch.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


public interface FeedstockBatchRepository {

  @NonNull
  FeedstockBatch save(@NonNull FeedstockBatch feedstockBatch);

  @NonNull
  Optional<@NonNull FeedstockBatch> findById(@NonNull FeedstockBatchId batchId);

  void delete(@NonNull FeedstockBatch feedstockBatch);
}
