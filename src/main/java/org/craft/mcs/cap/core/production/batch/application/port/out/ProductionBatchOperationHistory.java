package org.craft.mcs.cap.core.production.batch.application.port.out;

import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.jspecify.annotations.NonNull;


/** Production-owned query boundary for post-creation material operations. */
public interface ProductionBatchOperationHistory {

  boolean hasOperations(@NonNull ProductionBatchId batchId);
}
