package org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionResult;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Optimization result with graph interpretation aligned to every ranked candidate. */
public record CompositionOptimizationGraphView(
    @NonNull OptimizeCompositionResult optimization,
    @NonNull List<@NonNull CompositionOptimizationCandidateGraphView> candidates
) {

  public CompositionOptimizationGraphView {
    requireNonNull(optimization, "composition optimization result cannot be null");
    candidates = List.copyOf(requireNonNull(candidates, "graph candidate views cannot be null"));
    if (optimization.candidates().size() != candidates.size()) {
      throw new ApplicationValidationException(
          "graph candidate views must correspond to every optimized composition candidate"
      );
    }
  }
}
