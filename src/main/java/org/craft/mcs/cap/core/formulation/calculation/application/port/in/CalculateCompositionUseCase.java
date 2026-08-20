package org.craft.mcs.cap.core.formulation.calculation.application.port.in;

import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculateCompositionResult;
import org.jspecify.annotations.NonNull;


public interface CalculateCompositionUseCase {

  @NonNull
  CalculateCompositionResult calculate(@NonNull CalculateCompositionCommand command);
}
