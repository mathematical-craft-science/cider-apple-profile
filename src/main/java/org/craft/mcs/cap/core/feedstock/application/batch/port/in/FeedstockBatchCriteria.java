package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import java.util.Set;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchCriteria(
    @NonNull Set<@NonNull FeedstockBatchId> batchIds
) {

  public FeedstockBatchCriteria {
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    batchIds = Set.copyOf(batchIds);

    if (batchIds.isEmpty()) {
      throw new ApplicationValidationException("feedstock batch ids cannot be empty");
    }
  }

  public static @NonNull FeedstockBatchCriteria of(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return new FeedstockBatchCriteria(Set.of(batchId));
  }
}
