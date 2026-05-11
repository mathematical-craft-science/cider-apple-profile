package org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in;

import org.jspecify.annotations.NonNull;


public interface CalculateChemicalProfileCompositionUseCase {

  @NonNull
  CalculateChemicalProfileCompositionResult calculate(@NonNull CalculateChemicalProfileCompositionCommand command);
}
