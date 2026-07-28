package org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.model.ChemicalProfileCompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationCandidate;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationRequest;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application orchestrator for chemical profile composition optimization.
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
public final class OptimizeChemicalProfileCompositionService
    implements OptimizeChemicalProfileCompositionUseCase {

  private final @NonNull CompositionOptimizationStrategySelector optimizationStrategySelector;
  private final @NonNull ChemicalProfileCompositionCalculatorSelector calculatorSelector;
  private final @NonNull ProfileAxisNormalizerSelector normalizerSelector;
  private final @NonNull ProfileTargetObjectiveFunctionFactory objectiveFunctionFactory;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;

  @Override
  public @NonNull OptimizeChemicalProfileCompositionResult optimize(
      @NonNull OptimizeChemicalProfileCompositionCommand command
  ) {
    requireNonNull(command, "optimize chemical profile composition command cannot be null");

    final var calculator = calculatorSelector.select(command.calculatorCode());
    final var scoringConfiguration = command.scoringConfiguration();
    final var objectiveFunction = objectiveFunctionFactory.create(
        scoringConfiguration.objectiveFunctionCode(),
        normalizerSelector.select(scoringConfiguration.normalizerCode()),
        scoringConfiguration.weights()
    );
    final var optimizationStrategy = optimizationStrategySelector.select(command.algorithm());
    final var optimizationResult = optimizationStrategy.optimize(
        new ChemicalProfileCompositionOptimizationRequest(
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

    return new OptimizeChemicalProfileCompositionResult(
        command.mode(),
        command.algorithm(),
        command.calculatorCode(),
        scoringConfiguration.normalizerCode(),
        scoringConfiguration.objectiveFunctionCode(),
        optimizationResult.evaluatedCandidates(),
        optimizationResult.candidates().stream()
            .map(this::toCandidateView)
            .toList()
    );
  }

  private @NonNull ChemicalProfileCompositionOptimizationCandidateView toCandidateView(
      @NonNull ChemicalProfileCompositionOptimizationCandidate candidate
  ) {
    return new ChemicalProfileCompositionOptimizationCandidateView(
        candidate,
        potentialAbvPredictor.predict(candidate.composition().characteristics().specificGravity())
    );
  }
}
