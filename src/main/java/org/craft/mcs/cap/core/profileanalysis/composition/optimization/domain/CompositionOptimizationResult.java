package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain;

import java.util.List;
import org.jspecify.annotations.NonNull;
import static java.util.Objects.requireNonNull;


/**
 * Ranked optimization output.
 *
 * <p>{@code evaluatedCandidates} counts candidate compositions actually calculated and scored after
 * cheap pruning, such as source availability checks. {@code candidates} is already sorted by best
 * objective score first and truncated by the strategy settings.
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
