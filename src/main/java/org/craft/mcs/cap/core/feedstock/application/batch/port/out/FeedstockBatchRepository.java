package org.craft.mcs.cap.core.feedstock.application.batch.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;

public interface FeedstockBatchRepository
    extends VersionedAggregateRepository<FeedstockBatch, FeedstockBatchId> {

  /**
   * Loads a batch while serializing concurrent material-consumption attempts.
   *
   * <p>The locking mechanism is an adapter concern; the application contract expresses the
   * requirement that available volume must not be allocated concurrently from stale state.
   */
  @NonNull
  Optional<@NonNull VersionedAggregate<FeedstockBatch>> findByIdForConsumption(
      @NonNull FeedstockBatchId batchId
  );

  void delete(@NonNull VersionedAggregate<FeedstockBatch> feedstockBatch);
}
