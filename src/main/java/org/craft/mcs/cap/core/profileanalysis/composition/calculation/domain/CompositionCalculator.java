package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import org.jspecify.annotations.NonNull;


/**
 * Calculates typed resulting characteristics for a fixed source composition.
 *
 * <p>This interface is intentionally independent from optimization. An optimizer proposes source
 * weights; a calculator then applies the domain math that turns those weighted sources into one
 * {@link CalculatedComposition}. Different production domains may later select different
 * calculators through {@link CompositionCalculatorSelector}, while keeping the same
 * optimization and distance infrastructure.
 */
public interface CompositionCalculator {

  @NonNull
  CalculatedComposition calculate(@NonNull CompositionCalculationRequest request);
}
