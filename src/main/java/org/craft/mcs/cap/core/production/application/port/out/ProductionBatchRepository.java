package org.craft.mcs.cap.core.production.application.port.out;

import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.jspecify.annotations.NonNull;


public interface ProductionBatchRepository {

  @NonNull
  ProductionBatch save(@NonNull ProductionBatch productionBatch);
}
