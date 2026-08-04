package org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.model.CompositionOptimizationCandidateView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Ranked composition candidate enriched only with interpretation required for graph presentation. */
public record CompositionOptimizationCandidateGraphView(
    @NonNull CompositionOptimizationCandidateView candidate,
    @Nullable NorthAmericanTanninLevel northAmericanTanninLevel
) {

  public CompositionOptimizationCandidateGraphView {
    requireNonNull(candidate, "composition optimization candidate cannot be null");
  }
}
