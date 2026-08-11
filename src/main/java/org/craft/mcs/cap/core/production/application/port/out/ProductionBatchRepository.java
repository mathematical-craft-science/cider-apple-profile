package org.craft.mcs.cap.core.production.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;
import org.jspecify.annotations.NonNull;

public interface ProductionBatchRepository
    extends VersionedAggregateRepository<ProductionBatch, ProductionBatchId> {

  /**
   * Loads the batch under a shared row lock while a measurement change is committed.
   *
   * <p>The lock serializes measurement eligibility with material mutations of the same batch while
   * allowing independent measurement transactions to proceed concurrently.
   */
  @NonNull Optional<@NonNull ProductionBatch> lockForMeasurementChange(
      @NonNull ProductionBatchId batchId
  );
}
