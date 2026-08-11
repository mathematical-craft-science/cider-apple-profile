package org.craft.mcs.cap.core.production.application.port.out;

import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.jspecify.annotations.NonNull;


/** Production-owned query boundary for post-creation material operations. */
public interface ProductionBatchOperationHistory {

  boolean hasOperations(@NonNull ProductionBatchId batchId);
}
