package org.craft.mcs.cap.core.production.batch.application.port.in;

import org.craft.mcs.cap.core.production.batch.application.model.ProductionBatchDetailsView;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.jspecify.annotations.NonNull;


public interface GetProductionBatchUseCase {

  @NonNull
  ProductionBatchDetailsView get(@NonNull ProductionBatchId batchId);
}
