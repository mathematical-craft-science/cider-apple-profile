package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective;

import java.util.List;
import org.jspecify.annotations.NonNull;
import static java.util.Objects.requireNonNull;


/**
 * Result of evaluating one optional optimization criterion for one candidate.
 */
public record CompositionOptimizationCriterionAssessment(
    boolean candidateAllowed,
    @NonNull List<@NonNull CompositionOptimizationPenalty> penalties
) {

  public CompositionOptimizationCriterionAssessment {
    penalties = List.copyOf(requireNonNull(penalties, "composition optimization penalties cannot be null"));
  }

  public static @NonNull CompositionOptimizationCriterionAssessment allowed() {
    return new CompositionOptimizationCriterionAssessment(true, List.of());
  }

  public static @NonNull CompositionOptimizationCriterionAssessment withPenalty(
      @NonNull CompositionOptimizationPenalty penalty
  ) {
    requireNonNull(penalty, "composition optimization penalty cannot be null");
    return new CompositionOptimizationCriterionAssessment(true, List.of(penalty));
  }

  public static @NonNull CompositionOptimizationCriterionAssessment excluded() {
    return new CompositionOptimizationCriterionAssessment(false, List.of());
  }
}
