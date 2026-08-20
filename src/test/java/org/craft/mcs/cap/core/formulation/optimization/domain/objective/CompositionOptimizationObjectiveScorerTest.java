package org.craft.mcs.cap.core.formulation.optimization.domain.objective;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.domain.CalculatedComposition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


class CompositionOptimizationObjectiveScorerTest {

  private static final CalculatedComposition COMPOSITION = mock(CalculatedComposition.class);

  @Test
  void addsWarningPenaltyToObjectiveScore() {
    final var assessment = CompositionOptimizationObjectiveScorer.assess(
        COMPOSITION,
        new BigDecimal("0.25"),
        List.of(criterionWith(CompositionOptimizationPenaltySeverity.WARNING, "0.10"))
    );

    assertThat(assessment).hasValueSatisfying(result ->
        assertThat(result.score()).isEqualByComparingTo("0.35")
    );
  }

  @Test
  void preservesObjectiveScoreForInformationalPenalty() {
    final var assessment = CompositionOptimizationObjectiveScorer.assess(
        COMPOSITION,
        new BigDecimal("0.25"),
        List.of(criterionWith(CompositionOptimizationPenaltySeverity.INFO, "0.10"))
    );

    assertThat(assessment).hasValueSatisfying(result ->
        assertThat(result.score()).isEqualByComparingTo("0.25")
    );
  }

  @Test
  void excludesCandidateWithBlockingPenalty() {
    final var assessment = CompositionOptimizationObjectiveScorer.assess(
        COMPOSITION,
        new BigDecimal("0.25"),
        List.of(criterionWith(CompositionOptimizationPenaltySeverity.BLOCKING, "0.10"))
    );

    assertThat(assessment).isEmpty();
  }

  private static CompositionOptimizationCriterion criterionWith(
      CompositionOptimizationPenaltySeverity severity,
      String value
  ) {
    return ignored -> CompositionOptimizationCriterionAssessment.withPenalty(
        new CompositionOptimizationPenalty(
            CompositionOptimizationPenaltyCode.LOW_YEAST_ASSIMILABLE_NITROGEN,
            severity,
            new BigDecimal(value),
            null,
            null,
            null
        )
    );
  }
}
