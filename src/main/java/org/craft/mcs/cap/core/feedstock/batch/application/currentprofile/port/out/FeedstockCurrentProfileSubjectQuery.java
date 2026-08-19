package org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSubject;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


/** Resolves Feedstock batches and their ownership scopes for current-profile projection. */
public interface FeedstockCurrentProfileSubjectQuery {

  @NonNull
  List<@NonNull FeedstockCurrentProfileSubject> findByBatchIds(
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  );
}
