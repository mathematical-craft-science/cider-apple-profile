package org.craft.mcs.cap.core.feedstock.application.batch.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;
import org.jspecify.annotations.NonNull;

public interface FeedstockBatchRepository
    extends VersionedAggregateRepository<FeedstockBatch, FeedstockBatchId> {

  /** Locks one batch as the concurrency authority for a material mutation. */
  @NonNull Optional<@NonNull VersionedAggregate<FeedstockBatch>> lockVersionedById(
      @NonNull FeedstockBatchId batchId
  );

  /**
   * Loads the batch under a shared row lock while a measurement change is committed.
   *
   * <p>The lock serializes measurement eligibility with material mutations of the same batch while
   * allowing independent measurement transactions to proceed concurrently.
   */
  @NonNull Optional<@NonNull FeedstockBatch> lockForMeasurementChange(
      @NonNull FeedstockBatchId batchId
  );
}
