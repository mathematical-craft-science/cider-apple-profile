package org.craft.mcs.cap.core.formulation.optimization.application.contract;

import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition.Normalizer;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition.ObjectiveFunction;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;


class OptimizeCompositionResultTest {

  @Test
  void rejectsMoreReturnedCandidatesThanWereEvaluated() {
    final var candidate = mock(CompositionOptimizationCandidateView.class);
    final var candidates = List.of(candidate);

    assertThatThrownBy(() -> new OptimizeCompositionResult(
        CompositionOptimizationMode.PROPORTION,
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH,
        FormulationCalculator.MASS_BALANCE,
        Normalizer.EFFECTIVE_RANGE,
        ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
        CompositionOptimizationSettings.defaults(),
        0,
        candidates
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("returned composition candidate views cannot exceed evaluated candidate count");
  }

  @Test
  void rejectsMoreReturnedCandidatesThanRequestedLimit() {
    final var candidates = List.of(
        mock(CompositionOptimizationCandidateView.class),
        mock(CompositionOptimizationCandidateView.class)
    );

    assertThatThrownBy(() -> new OptimizeCompositionResult(
        CompositionOptimizationMode.PROPORTION,
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH,
        FormulationCalculator.MASS_BALANCE,
        Normalizer.EFFECTIVE_RANGE,
        ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
        new CompositionOptimizationSettings(5, 1),
        2,
        candidates
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("returned composition candidate views cannot exceed the requested result limit");
  }
}
