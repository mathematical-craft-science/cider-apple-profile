package org.craft.mcs.cap.core.formulation.interpretation.application.contract;

import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculateCompositionResult;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Composition calculation enriched only with interpretation required for graph presentation. */
public record CalculatedCompositionGraphView(
    @NonNull CalculateCompositionResult calculation,
    @Nullable FormulationTanninLevel northAmericanTanninLevel
) {

  public CalculatedCompositionGraphView {
    requireNonNull(calculation, "composition calculation cannot be null");
    final var tannin = calculation.composition().characteristics().tannin();
    if ((tannin == null) != (northAmericanTanninLevel == null)) {
      throw new ApplicationValidationException(
          "North American tannin level must be present exactly when composition tannin is available"
      );
    }
  }
}
