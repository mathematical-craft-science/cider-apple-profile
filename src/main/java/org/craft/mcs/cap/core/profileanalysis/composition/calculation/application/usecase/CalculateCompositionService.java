package org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionUseCase;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorSelector;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculationRequest;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CalculatedComposition;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetIntervalPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for calculating one explicitly provided composition.
 *
 * <p>This use case differs from optimization: source weights are already known. The service selects
 * the requested calculator, predicts potential ABV from the resulting SG and, when a target
 * is provided, also builds the configured normalizer and objective function to explain distance from
 * the calculated composition to the target.
 */
@Component
@RequiredArgsConstructor
public final class CalculateCompositionService implements CalculateCompositionUseCase {

  private final @NonNull CompositionCalculatorSelector calculatorSelector;
  private final @NonNull ProfileAxisNormalizerSelector normalizerSelector;
  private final @NonNull ProfileTargetObjectiveFunctionFactory objectiveFunctionFactory;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;

  @Override
  public @NonNull CalculateCompositionResult calculate(
      @NonNull CalculateCompositionCommand command
  ) {
    requireNonNull(command, "calculate composition command cannot be null");
    final var calculator = calculatorSelector.select(command.calculatorCode());
    final var composition = calculator.calculate(new CompositionCalculationRequest(
        command.sources(),
        command.tanninPolicy()
    ));
    final var potentialAbv = potentialAbvPredictor.predict(composition.characteristics().specificGravity());
    final var targetObjective = command.targetObjective();
    if (targetObjective == null) {
      return CalculateCompositionResult.withoutTargetDistance(composition, potentialAbv);
    }

    final var targetDistance = distanceToTarget(
        composition,
        targetObjective,
        command.scoringConfiguration()
    );
    return new CalculateCompositionResult(
        composition,
        potentialAbv,
        command.scoringConfiguration().normalizerCode(),
        targetDistance
    );
  }

  private @NonNull ProfileTargetDistance distanceToTarget(
      @NonNull CalculatedComposition composition,
      @NonNull ProfileTargetObjective targetObjective,
      @NonNull ProfileTargetScoringConfiguration scoringConfiguration
  ) {
    final var objectiveFunction = objectiveFunctionFactory.create(
        scoringConfiguration.objectiveFunctionCode(),
        normalizerSelector.select(scoringConfiguration.normalizerCode()),
        scoringConfiguration.weights()
    );
    final var characteristics = composition.characteristics();
    return objectiveFunction.distanceTo(
        ProfileTargetScoringInput.point(
            characteristics.specificGravity(),
            characteristics.titratableAcidity(),
            characteristics.tannin()
        ),
        ProfileTargetIntervalPolicy.MIDPOINT,
        targetObjective
    );
  }
}
