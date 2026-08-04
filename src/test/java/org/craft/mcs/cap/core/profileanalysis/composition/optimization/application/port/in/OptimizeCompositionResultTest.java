package org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.model.CompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
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
        CompositionCalculatorCode.MASS_BALANCE,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        DiscretizedCompositionOptimizationSettings.defaults(),
        0,
        candidates
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("returned composition candidate views cannot exceed evaluated candidate count");
  }
}
