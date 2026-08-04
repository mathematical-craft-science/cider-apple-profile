package org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Composition calculation enriched only with interpretation required for graph presentation. */
public record CalculatedCompositionGraphView(
    @NonNull CalculateCompositionResult calculation,
    @Nullable NorthAmericanTanninLevel northAmericanTanninLevel
) {

  public CalculatedCompositionGraphView {
    requireNonNull(calculation, "composition calculation cannot be null");
  }
}
