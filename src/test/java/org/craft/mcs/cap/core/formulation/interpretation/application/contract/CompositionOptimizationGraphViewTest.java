package org.craft.mcs.cap.core.formulation.interpretation.application.contract;

import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculatedCompositionView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition.Normalizer;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition.ObjectiveFunction;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationMode;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationSettings;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizeCompositionResult;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class CompositionOptimizationGraphViewTest {

  @Test
  void acceptsGraphCandidatesInOptimizationRankingOrder() {
    final var first = candidate(null);
    final var second = candidate(null);
    final var optimization = optimization(first, second);
    final var graphCandidates = List.of(graph(first, null), graph(second, null));

    assertThat(new CompositionOptimizationGraphView(optimization, graphCandidates).candidates())
        .containsExactlyElementsOf(graphCandidates);
  }

  @Test
  void rejectsGraphCandidatesThatDoNotMatchOptimizationRankingOrder() {
    final var first = candidate(null);
    final var second = candidate(null);
    final var optimization = optimization(first, second);

    assertThatThrownBy(() -> new CompositionOptimizationGraphView(
        optimization,
        List.of(graph(second, null), graph(first, null))
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage(
            "graph candidate views must correspond to optimized composition candidates in ranking order"
        );
  }

  @Test
  void rejectsTanninLevelPresenceThatDoesNotMatchCandidateTannin() {
    assertThatThrownBy(() -> graph(candidate("1800"), null))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage(
            "North American tannin level must be present exactly when candidate tannin is available"
        );
    assertThatThrownBy(() -> graph(candidate(null), FormulationTanninLevel.MEDIUM))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage(
            "North American tannin level must be present exactly when candidate tannin is available"
        );
  }

  private static @NonNull CompositionOptimizationCandidateView candidate(@Nullable String tannin) {
    final var candidate = mock(CompositionOptimizationCandidateView.class);
    when(candidate.composition()).thenReturn(new CalculatedCompositionView(
        List.of(),
        new CalculatedCompositionView.Characteristics(
            SpecificGravityValue.of("1.060"),
            TitratableAcidityValue.of("5.0"),
            tannin == null ? null : TanninValue.of(tannin),
            null
        ),
        null,
        List.of()
    ));
    return candidate;
  }

  private static @NonNull CompositionOptimizationCandidateGraphView graph(
      @NonNull CompositionOptimizationCandidateView candidate,
      @Nullable FormulationTanninLevel tanninLevel
  ) {
    return new CompositionOptimizationCandidateGraphView(candidate, tanninLevel);
  }

  private static @NonNull OptimizeCompositionResult optimization(
      @NonNull CompositionOptimizationCandidateView @NonNull ... candidates
  ) {
    return new OptimizeCompositionResult(
        CompositionOptimizationMode.PROPORTION,
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH,
        FormulationCalculator.MASS_BALANCE,
        Normalizer.EFFECTIVE_RANGE,
        ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
        CompositionOptimizationSettings.defaults(),
        candidates.length,
        List.of(candidates)
    );
  }
}
