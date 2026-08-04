package org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.service;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CalculatedComposition;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.model.CompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model.CalculatedCompositionGraphView;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model.CompositionOptimizationCandidateGraphView;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model.CompositionOptimizationGraphView;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.port.in.CompositionGraphInterpretationUseCase;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
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

  private final @NonNull NorthAmericanProfileCalculator northAmericanProfileCalculator;

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
        northAmericanTanninLevel(candidate.candidate().composition())
    );
  }

  private @Nullable NorthAmericanTanninLevel northAmericanTanninLevel(
      @NonNull CalculatedComposition composition
  ) {
    final var tannin = composition.characteristics().tannin();
    return tannin == null
        ? null
        : northAmericanProfileCalculator.assessTannin(PointCharacteristicValue.of(tannin));
  }
}
