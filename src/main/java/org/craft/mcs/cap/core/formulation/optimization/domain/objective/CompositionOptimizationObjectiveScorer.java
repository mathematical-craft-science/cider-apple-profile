package org.craft.mcs.cap.core.formulation.optimization.domain.objective;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.formulation.calculation.domain.CalculatedComposition;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import static java.util.Objects.requireNonNull;


/**
 * Combines profile-target distance with optional production-readiness criteria into one rankable
 * objective score.
 */
public final class CompositionOptimizationObjectiveScorer {

  private CompositionOptimizationObjectiveScorer() {
  }

  public static @NonNull Optional<@NonNull CompositionOptimizationObjectiveAssessment> assess(
      @NonNull CalculatedComposition composition,
      @Nullable BigDecimal targetDistance,
      @NonNull List<@NonNull CompositionOptimizationCriterion> criteria
  ) {
    requireNonNull(composition, "composition cannot be null");
    requireNonNull(criteria, "composition optimization criteria cannot be null");

    final var penalties = new ArrayList<CompositionOptimizationPenalty>();
    for (final var criterion : criteria) {
      final var assessment = criterion.assess(composition);
      if (!assessment.candidateAllowed()) {
        return Optional.empty();
      }
      penalties.addAll(assessment.penalties());
      if (assessment.penalties().stream()
          .anyMatch(penalty -> penalty.severity() == CompositionOptimizationPenaltySeverity.BLOCKING)) {
        return Optional.empty();
      }
    }

    return Optional.of(new CompositionOptimizationObjectiveAssessment(
        score(targetDistance, penalties),
        penalties
    ));
  }

  private static @Nullable BigDecimal score(
      @Nullable BigDecimal targetDistance,
      @NonNull List<@NonNull CompositionOptimizationPenalty> penalties
  ) {
    if (targetDistance == null) {
      return null;
    }

    var score = targetDistance;
    for (final var penalty : penalties) {
      if (penalty.severity() == CompositionOptimizationPenaltySeverity.WARNING) {
        score = score.add(penalty.value());
      }
    }
    return score;
  }
}
