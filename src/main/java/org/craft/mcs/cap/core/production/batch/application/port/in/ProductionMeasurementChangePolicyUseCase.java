package org.craft.mcs.cap.core.production.batch.application.port.in;

import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.jspecify.annotations.NonNull;


/** Production boundary that validates whether measurement evidence may be changed. */
public interface ProductionMeasurementChangePolicyUseCase {

  void requireChangesAllowed(@NonNull ProductionBatchId batchId);
}
