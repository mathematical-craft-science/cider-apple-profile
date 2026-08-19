package org.craft.mcs.cap.core.feedstock.planning.application.candidate.model;

import java.util.Set;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Planning-owned selection of Feedstock batches. */
public record FeedstockPlanningBatchCriteria(
    @NonNull Set<@NonNull FeedstockBatchId> batchIds
) {

  public FeedstockPlanningBatchCriteria {
    batchIds = Set.copyOf(requireNonNull(batchIds, "feedstock planning batch ids cannot be null"));
    if (batchIds.isEmpty()) {
      throw new ApplicationValidationException("feedstock planning batch ids cannot be empty");
    }
  }

  public static @NonNull FeedstockPlanningBatchCriteria of(@NonNull FeedstockBatchId batchId) {
    return new FeedstockPlanningBatchCriteria(Set.of(requireNonNull(batchId, "feedstock batch id cannot be null")));
  }
}
