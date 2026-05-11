package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.jspecify.annotations.NonNull;


public interface OptimizeFeedstockBatchCompositionUseCase {

  @NonNull
  OptimizeFeedstockBatchCompositionResult optimize(@NonNull OptimizeFeedstockBatchCompositionCommand command);
}
