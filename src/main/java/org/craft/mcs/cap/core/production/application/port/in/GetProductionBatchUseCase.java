package org.craft.mcs.cap.core.production.application.port.in;

import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.jspecify.annotations.NonNull;


public interface GetProductionBatchUseCase {

  @NonNull
  ProductionBatchView get(@NonNull ProductionBatchId batchId);
}
