package org.craft.mcs.cap.core.feedstock.application.inventory.port.in;

import org.jspecify.annotations.NonNull;


/** Inbound boundary for atomically consuming feedstock material with its current profile evidence. */
public interface ConsumeFeedstockBatchMaterialUseCase {

  @NonNull
  ConsumedFeedstockBatchMaterial consume(@NonNull ConsumeFeedstockBatchMaterialCommand command);
}
