package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import org.jspecify.annotations.NonNull;


public interface OptimizeChemicalProfileCompositionUseCase {

  @NonNull
  OptimizeChemicalProfileCompositionResult optimize(
      @NonNull OptimizeChemicalProfileCompositionCommand command
  );
}
