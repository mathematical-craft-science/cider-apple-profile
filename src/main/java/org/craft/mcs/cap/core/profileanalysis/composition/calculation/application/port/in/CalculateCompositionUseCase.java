package org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in;

import org.jspecify.annotations.NonNull;


public interface CalculateCompositionUseCase {

  @NonNull
  CalculateCompositionResult calculate(@NonNull CalculateCompositionCommand command);
}
