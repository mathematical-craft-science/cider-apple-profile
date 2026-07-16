package org.craft.mcs.cap.core.feedstock.application.inventory.port.in;

import org.jspecify.annotations.NonNull;


/** Provides authoritative current feedstock evidence without consuming its volume. */
public interface InspectFeedstockBatchMaterialUseCase {

  @NonNull FeedstockBatchMaterialSnapshot inspect(@NonNull InspectFeedstockBatchMaterialCommand command);
}
