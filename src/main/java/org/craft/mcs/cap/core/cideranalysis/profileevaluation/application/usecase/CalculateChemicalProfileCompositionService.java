package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.CalculateChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.CalculateChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.CalculateChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionRequest;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculator;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileComposition;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.DistanceToTargetAreaObjectiveFunction;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveWeights;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class CalculateChemicalProfileCompositionService implements CalculateChemicalProfileCompositionUseCase {

  private final @NonNull ChemicalProfileCompositionCalculator calculator;
  private final @NonNull ProfileAxisNormalizerSelector normalizerSelector;
  private final @NonNull ProfileTargetScoringPurposeNormalizerPolicy normalizerPolicy;

  @Override
  public @NonNull CalculateChemicalProfileCompositionResult calculate(
      @NonNull CalculateChemicalProfileCompositionCommand command
  ) {
    requireNonNull(command, "calculate chemical profile composition command cannot be null");
    final var composition = calculator.calculate(new ChemicalProfileCompositionRequest(
        command.sources(),
        command.tanninPolicy()
    ));
    final var targetObjective = command.targetObjective();
    if (targetObjective == null) {
      return CalculateChemicalProfileCompositionResult.withoutTargetDistance(composition);
    }

    final var normalizerCode = normalizerPolicy.normalizerFor(command.scoringPurpose());
    final var targetDistance = distanceToTarget(
        composition,
        targetObjective,
        command.targetWeights(),
        normalizerCode
    );
    return new CalculateChemicalProfileCompositionResult(
        composition,
        normalizerCode,
        targetDistance
    );
  }

  private @NonNull ProfileTargetDistance distanceToTarget(
      @NonNull ChemicalProfileComposition composition,
      @NonNull ProfileTargetObjective targetObjective,
      @NonNull ProfileTargetObjectiveWeights targetWeights,
      @NonNull ProfileAxisNormalizerCode normalizerCode
  ) {
    final var normalizer = normalizerSelector.select(normalizerCode);
    final var objectiveFunction = new DistanceToTargetAreaObjectiveFunction(
        normalizer,
        targetWeights
    );
    return objectiveFunction.distanceTo(composition.profile(), targetObjective);
  }
}
