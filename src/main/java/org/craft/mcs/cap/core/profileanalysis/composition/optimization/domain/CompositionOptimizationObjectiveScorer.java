package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileComposition;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetDistance;
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
      @NonNull ChemicalProfileComposition composition,
      @NonNull ProfileTargetDistance targetDistance,
      @NonNull List<@NonNull CompositionOptimizationCriterion> criteria
  ) {
    requireNonNull(composition, "chemical profile composition cannot be null");
    requireNonNull(targetDistance, "profile target distance cannot be null");
    requireNonNull(criteria, "composition optimization criteria cannot be null");

    final var penalties = new ArrayList<CompositionOptimizationPenalty>();
    for (final var criterion : criteria) {
      final var assessment = criterion.assess(composition);
      if (!assessment.candidateAllowed()) {
        return Optional.empty();
      }
      penalties.addAll(assessment.penalties());
    }

    return Optional.of(new CompositionOptimizationObjectiveAssessment(
        score(targetDistance, penalties),
        penalties
    ));
  }

  private static @Nullable BigDecimal score(
      @NonNull ProfileTargetDistance targetDistance,
      @NonNull List<@NonNull CompositionOptimizationPenalty> penalties
  ) {
    final var targetDistanceValue = targetDistance.value();
    if (targetDistanceValue == null) {
      return null;
    }

    var score = targetDistanceValue;
    for (final var penalty : penalties) {
      score = score.add(penalty.value());
    }
    return score;
  }
}
