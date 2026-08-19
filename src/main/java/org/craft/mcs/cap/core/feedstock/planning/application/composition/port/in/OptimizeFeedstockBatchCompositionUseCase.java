package org.craft.mcs.cap.core.feedstock.planning.application.composition.port.in;

import org.jspecify.annotations.NonNull;


public interface OptimizeFeedstockBatchCompositionUseCase {

  @NonNull
  OptimizeFeedstockBatchCompositionResult optimize(@NonNull OptimizeFeedstockBatchCompositionCommand command);
}
