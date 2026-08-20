package org.craft.mcs.cap.core.formulation.optimization.application.port.in;

import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizeCompositionResult;
import org.jspecify.annotations.NonNull;


public interface OptimizeCompositionUseCase {

  @NonNull
  OptimizeCompositionResult optimize(
      @NonNull OptimizeCompositionCommand command
  );
}
