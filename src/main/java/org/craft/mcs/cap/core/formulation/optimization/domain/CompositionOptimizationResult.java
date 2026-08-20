package org.craft.mcs.cap.core.formulation.optimization.domain;

import java.util.List;
import org.jspecify.annotations.NonNull;
import static java.util.Objects.requireNonNull;


/**
 * Candidate-generation output produced by an optimization strategy.
 *
 * <p>{@code evaluatedCandidates} counts candidate compositions actually calculated after cheap
 * pruning, such as source availability checks. Target scoring, readiness penalties, ranking, and
 * result limiting are application orchestration responsibilities.
 */
public record CompositionOptimizationResult(
    int evaluatedCandidates,
    @NonNull List<@NonNull CompositionOptimizationCandidate> candidates
) {

  public CompositionOptimizationResult {
    if (evaluatedCandidates < 0) {
      throw new InvalidCompositionOptimizationException(
          "evaluated composition candidate count cannot be negative"
      );
    }
    candidates = List.copyOf(requireNonNull(
        candidates,
        "composition optimization candidates cannot be null"
    ));
    if (candidates.size() > evaluatedCandidates) {
      throw new InvalidCompositionOptimizationException(
          "returned composition candidates cannot exceed evaluated candidate count"
      );
    }
  }
}
