package org.craft.mcs.cap.core.formulation.calculation.domain;

import org.jspecify.annotations.NonNull;


/**
 * Selects the calculator used to transform weighted sources into a composition.
 *
 * <p>This is the composition-calculation extension point. Optimization can keep the same search
 * algorithm and target distance function while replacing the underlying chemistry calculation when
 * the domain model becomes richer.
 */
public interface CompositionCalculatorSelector {

  @NonNull
  CompositionCalculator select(@NonNull CompositionCalculatorCode code);
}
