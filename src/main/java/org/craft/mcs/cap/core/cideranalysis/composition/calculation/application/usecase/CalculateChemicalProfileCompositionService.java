package org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionRequest;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileComposition;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
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
