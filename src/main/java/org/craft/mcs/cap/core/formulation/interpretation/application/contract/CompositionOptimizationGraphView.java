package org.craft.mcs.cap.core.formulation.interpretation.application.contract;

import java.util.List;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizeCompositionResult;
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
    final var interpretedCandidates = candidates.stream()
        .map(CompositionOptimizationCandidateGraphView::candidate)
        .toList();
    if (!optimization.candidates().equals(interpretedCandidates)) {
      throw new ApplicationValidationException(
          "graph candidate views must correspond to optimized composition candidates in ranking order"
      );
    }
  }
}
