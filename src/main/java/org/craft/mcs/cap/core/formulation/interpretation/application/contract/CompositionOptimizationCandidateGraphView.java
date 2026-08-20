package org.craft.mcs.cap.core.formulation.interpretation.application.contract;

import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationCandidateView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Ranked composition candidate enriched only with interpretation required for graph presentation. */
public record CompositionOptimizationCandidateGraphView(
    @NonNull CompositionOptimizationCandidateView candidate,
    @Nullable FormulationTanninLevel northAmericanTanninLevel
) {

  public CompositionOptimizationCandidateGraphView {
    requireNonNull(candidate, "composition optimization candidate cannot be null");
    final var tannin = candidate.composition().characteristics().tannin();
    if ((tannin == null) != (northAmericanTanninLevel == null)) {
      throw new ApplicationValidationException(
          "North American tannin level must be present exactly when candidate tannin is available"
      );
    }
  }
}
