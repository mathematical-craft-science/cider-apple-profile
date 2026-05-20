package org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Ranked optimization output.
 *
 * <p>{@code evaluatedCandidates} counts candidate compositions actually calculated and scored after
 * cheap pruning, such as source availability checks. {@code candidates} is already sorted by best
 * distance first and truncated by the strategy settings.
 */
public record ChemicalProfileCompositionOptimizationResult(
    int evaluatedCandidates,
    @NonNull List<@NonNull ChemicalProfileCompositionOptimizationCandidate> candidates
) {

  public ChemicalProfileCompositionOptimizationResult {
    if (evaluatedCandidates < 0) {
      throw new InvalidChemicalProfileCompositionOptimizationException("evaluated composition candidate count cannot be negative");
    }
    candidates = List.copyOf(requireNonNull(candidates, "chemical profile composition optimization candidates cannot be null"));
  }
}
