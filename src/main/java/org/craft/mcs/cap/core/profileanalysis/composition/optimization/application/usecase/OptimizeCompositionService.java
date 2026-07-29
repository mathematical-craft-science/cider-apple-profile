package org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.model.CompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionUseCase;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorSelector;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationCandidate;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationRequest;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application orchestrator for composition optimization.
 *
 * <p>The service wires together four independently selectable concerns:
 * <ol>
 *   <li>composition calculator, selected by {@code calculatorCode};</li>
 *   <li>axis normalizer, selected by the explicit scoring configuration;</li>
 *   <li>objective function and axis weights from the same scoring configuration;</li>
 *   <li>optimization algorithm, selected by {@code algorithm}.</li>
 * </ol>
 *
 * <p>The orchestration lives in the application layer because it combines several domain
 * strategies for a concrete use case. The domain strategies remain independently testable and do
 * not know about REST, DTO defaults, or user-facing scenario names.
 */
@Component
@RequiredArgsConstructor
public final class OptimizeCompositionService
    implements OptimizeCompositionUseCase {

  private final @NonNull CompositionOptimizationStrategySelector optimizationStrategySelector;
  private final @NonNull CompositionCalculatorSelector calculatorSelector;
  private final @NonNull ProfileAxisNormalizerSelector normalizerSelector;
  private final @NonNull ProfileTargetObjectiveFunctionFactory objectiveFunctionFactory;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;

  @Override
  public @NonNull OptimizeCompositionResult optimize(
      @NonNull OptimizeCompositionCommand command
  ) {
    requireNonNull(command, "optimize composition command cannot be null");

    final var calculator = calculatorSelector.select(command.calculatorCode());
    final var scoringConfiguration = command.scoringConfiguration();
    final var objectiveFunction = objectiveFunctionFactory.create(
        scoringConfiguration.objectiveFunctionCode(),
        normalizerSelector.select(scoringConfiguration.normalizerCode()),
        scoringConfiguration.weights()
    );
    final var optimizationStrategy = optimizationStrategySelector.select(command.algorithm());
    final var optimizationResult = optimizationStrategy.optimize(
        new CompositionOptimizationRequest(
            command.sources(),
            command.tanninPolicy(),
            command.targetObjective(),
            objectiveFunction,
            command.criteria(),
            command.settings(),
            command.mode(),
            command.targetTotalVolume()
        ),
        calculator
    );

    return new OptimizeCompositionResult(
        command.mode(),
        command.algorithm(),
        command.calculatorCode(),
        scoringConfiguration.normalizerCode(),
        scoringConfiguration.objectiveFunctionCode(),
        command.settings(),
        optimizationResult.evaluatedCandidates(),
        optimizationResult.candidates().stream()
            .map(this::toCandidateView)
            .toList()
    );
  }

  private @NonNull CompositionOptimizationCandidateView toCandidateView(
      @NonNull CompositionOptimizationCandidate candidate
  ) {
    return new CompositionOptimizationCandidateView(
        candidate,
        potentialAbvPredictor.predict(candidate.composition().characteristics().specificGravity())
    );
  }
}
