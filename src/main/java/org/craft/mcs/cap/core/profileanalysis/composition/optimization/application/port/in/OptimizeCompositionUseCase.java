package org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in;

import org.jspecify.annotations.NonNull;


public interface OptimizeCompositionUseCase {

  @NonNull
  OptimizeCompositionResult optimize(
      @NonNull OptimizeCompositionCommand command
  );
}
