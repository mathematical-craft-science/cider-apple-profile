package org.craft.mcs.cap.core.feedstock.application.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.CiderMakerId;
import org.jspecify.annotations.NonNull;


public interface FeedstockBatchRepository {

  @NonNull
  FeedstockBatch save(@NonNull FeedstockBatch feedstockBatch);

  @NonNull
  Optional<@NonNull FeedstockBatch> findById(@NonNull FeedstockBatchId batchId);

  @NonNull
  List<@NonNull FeedstockBatch> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId);
}
