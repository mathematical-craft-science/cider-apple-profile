package org.craft.mcs.cap.core.formulation.interpretation.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculatedCompositionView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculateCompositionResult;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizeCompositionResult;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.CalculatedCompositionGraphView;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.CompositionOptimizationCandidateGraphView;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.CompositionOptimizationGraphView;
import org.craft.mcs.cap.core.formulation.interpretation.application.port.in.CompositionGraphInterpretationUseCase;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.FormulationTanninLevel;
import org.craft.mcs.cap.core.formulation.interpretation.application.port.out.FormulationTanninLevelAnalyzer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Adds North American tannin interpretation to calculated compositions used as graph points.
 *
 * <p>This higher-level application projection keeps composition mathematics independent from the
 * classification system. The interpretation is descriptive and never changes target distance or
 * optimization ranking.</p>
 */
@Component
@RequiredArgsConstructor
public final class CompositionGraphInterpretationService implements CompositionGraphInterpretationUseCase {

  private final @NonNull FormulationTanninLevelAnalyzer tanninLevelAnalyzer;

  @Override
  public @NonNull CalculatedCompositionGraphView interpret(
      @NonNull CalculateCompositionResult calculation
  ) {
    requireNonNull(calculation, "composition calculation cannot be null");
    return new CalculatedCompositionGraphView(
        calculation,
        northAmericanTanninLevel(calculation.composition())
    );
  }

  @Override
  public @NonNull CompositionOptimizationGraphView interpret(
      @NonNull OptimizeCompositionResult optimization
  ) {
    requireNonNull(optimization, "composition optimization result cannot be null");
    return new CompositionOptimizationGraphView(
        optimization,
        optimization.candidates().stream()
            .map(this::interpret)
            .toList()
    );
  }

  private @NonNull CompositionOptimizationCandidateGraphView interpret(
      @NonNull CompositionOptimizationCandidateView candidate
  ) {
    return new CompositionOptimizationCandidateGraphView(
        candidate,
        northAmericanTanninLevel(candidate.composition())
    );
  }

  private @Nullable FormulationTanninLevel northAmericanTanninLevel(
      @NonNull CalculatedCompositionView composition
  ) {
    final var tannin = composition.characteristics().tannin();
    return tannin == null
        ? null
        : tanninLevelAnalyzer.assess(tannin);
  }

}
