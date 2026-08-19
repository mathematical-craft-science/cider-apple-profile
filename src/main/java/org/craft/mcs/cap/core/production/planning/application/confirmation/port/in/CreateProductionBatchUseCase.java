package org.craft.mcs.cap.core.production.planning.application.confirmation.port.in;

import org.jspecify.annotations.NonNull;


/** Creates a ProductionBatch only by explicitly confirming an authoritative saved candidate. */
public interface CreateProductionBatchUseCase {

  @NonNull
  ProductionBatchCreated create(@NonNull CreateProductionBatchCommand command);
}
