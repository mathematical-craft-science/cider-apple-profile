package org.craft.mcs.cap.core.cideranalysis.composition.optimization.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationRequest;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.CompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application orchestrator for chemical profile composition optimization.
 *
 * <p>The service wires together four independently selectable concerns:
 * <ol>
 *   <li>composition calculator, selected by {@code calculatorCode};</li>
 *   <li>axis normalizer, selected by {@code normalizerCode};</li>
 *   <li>objective function, selected by {@code objectiveFunctionCode};</li>
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

  @Override
  public @NonNull OptimizeChemicalProfileCompositionResult optimize(
      @NonNull OptimizeChemicalProfileCompositionCommand command
  ) {
    requireNonNull(command, "optimize chemical profile composition command cannot be null");

    final var calculator = calculatorSelector.select(command.calculatorCode());
    final var objectiveFunction = objectiveFunctionFactory.create(
        command.objectiveFunctionCode(),
        normalizerSelector.select(command.normalizerCode()),
        command.targetWeights()
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
        command.normalizerCode(),
        command.objectiveFunctionCode(),
        optimizationResult
    );
  }
}
