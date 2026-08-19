package org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


/** Reads state-aware composite current profiles without exposing persistence details. */
public interface FeedstockCurrentProfileQueryRepository {

  @NonNull
  Optional<@NonNull FeedstockCurrentProfileView> findByBatchId(@NonNull FeedstockBatchId batchId);

  @NonNull
  List<@NonNull FeedstockCurrentProfileView> findByBatchIds(
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  );
}
