package org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain;

import org.jspecify.annotations.NonNull;


/**
 * Selects the calculator used to transform weighted sources into a chemical profile composition.
 *
 * <p>This is the composition-calculation extension point. Optimization can keep the same search
 * algorithm and target distance function while replacing the underlying chemistry calculation when
 * the domain model becomes richer.
 */
public interface ChemicalProfileCompositionCalculatorSelector {

  @NonNull
  ChemicalProfileCompositionCalculator select(@NonNull ChemicalProfileCompositionCalculatorCode code);
}
