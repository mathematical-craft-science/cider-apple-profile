package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition;

import org.jspecify.annotations.NonNull;


public interface ChemicalProfileCompositionCalculator {

  @NonNull
  ChemicalProfileComposition calculate(@NonNull ChemicalProfileCompositionRequest request);
}
