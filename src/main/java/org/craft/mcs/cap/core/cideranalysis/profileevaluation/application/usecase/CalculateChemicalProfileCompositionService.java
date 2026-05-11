package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.CalculateChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.CalculateChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.CalculateChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionRequest;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileComposition;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveWeights;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for calculating one explicitly provided chemical profile composition.
 *
 * <p>This use case differs from optimization: source weights are already known. The service selects
 * the requested calculator and, when a target is provided, also builds the configured normalizer and
 * objective function to explain distance from the calculated composition to the target.
 */
@Component
@RequiredArgsConstructor
public final class CalculateChemicalProfileCompositionService implements CalculateChemicalProfileCompositionUseCase {

  private final @NonNull ChemicalProfileCompositionCalculatorSelector calculatorSelector;
  private final @NonNull ProfileAxisNormalizerSelector normalizerSelector;
  private final @NonNull ProfileTargetObjectiveFunctionFactory objectiveFunctionFactory;

  @Override
  public @NonNull CalculateChemicalProfileCompositionResult calculate(
      @NonNull CalculateChemicalProfileCompositionCommand command
  ) {
    requireNonNull(command, "calculate chemical profile composition command cannot be null");
    final var calculator = calculatorSelector.select(command.calculatorCode());
    final var composition = calculator.calculate(new ChemicalProfileCompositionRequest(
        command.sources(),
        command.tanninPolicy()
    ));
    final var targetObjective = command.targetObjective();
    if (targetObjective == null) {
      return CalculateChemicalProfileCompositionResult.withoutTargetDistance(composition);
    }

    final var targetDistance = distanceToTarget(
        composition,
        targetObjective,
        command.targetWeights(),
        command.normalizerCode(),
        command.objectiveFunctionCode()
    );
    return new CalculateChemicalProfileCompositionResult(
        composition,
        command.normalizerCode(),
        targetDistance
    );
  }

  private @NonNull ProfileTargetDistance distanceToTarget(
      @NonNull ChemicalProfileComposition composition,
      @NonNull ProfileTargetObjective targetObjective,
      @NonNull ProfileTargetObjectiveWeights targetWeights,
      @NonNull ProfileAxisNormalizerCode normalizerCode,
      @NonNull ProfileTargetObjectiveFunctionCode objectiveFunctionCode
  ) {
    final var objectiveFunction = objectiveFunctionFactory.create(
        objectiveFunctionCode,
        normalizerSelector.select(normalizerCode),
        targetWeights
    );
    return objectiveFunction.distanceTo(composition.profile(), targetObjective);
  }
}
