package org.craft.mcs.cap.core.production.application.port.in;

import org.jspecify.annotations.NonNull;


public interface CreateProductionBatchUseCase {

  @NonNull
  ProductionBatchCreated create(@NonNull CreateProductionBatchCommand command);
}
