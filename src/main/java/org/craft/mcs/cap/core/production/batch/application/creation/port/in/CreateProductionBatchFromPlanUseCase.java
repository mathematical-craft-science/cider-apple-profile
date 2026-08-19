package org.craft.mcs.cap.core.production.batch.application.creation.port.in;

import org.jspecify.annotations.NonNull;

/** Creates production material from an already accepted and immutable planning snapshot. */
public interface CreateProductionBatchFromPlanUseCase {

  @NonNull CreatedProductionBatch create(@NonNull CreateProductionBatchFromPlanCommand command);
}
