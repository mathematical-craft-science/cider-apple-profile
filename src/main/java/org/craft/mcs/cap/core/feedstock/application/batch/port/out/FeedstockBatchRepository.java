package org.craft.mcs.cap.core.feedstock.application.batch.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;


public interface FeedstockBatchRepository {

  @NonNull
  FeedstockBatch save(@NonNull FeedstockBatch feedstockBatch);

  @NonNull
  Optional<@NonNull FeedstockBatch> findById(@NonNull FeedstockBatchId batchId);

  @NonNull
  List<@NonNull FeedstockBatch> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId);

  void delete(@NonNull FeedstockBatch feedstockBatch);
}
