package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.targetfit;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityZoneCode;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.TargetFitTargetUnavailableException;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateReference;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.Distance;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitScoringConfiguration;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.ScoreTargetFitUseCase;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.TargetFitCommand;
import org.craft.mcs.cap.core.formulation.calculation.application.FormulationTargetFitUnavailableException;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculatedCompositionView.Characteristics;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition.ScoringConfiguration;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition.TargetArea;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView;
import org.craft.mcs.cap.core.formulation.calculation.application.port.out.FormulationTargetFitAnalyzer;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/**
 * ACL from Formulation target language to CiderAnalysis target-fit scoring.
 */
@Component
@RequiredArgsConstructor
public final class FormulationTargetFitAnalysisAdapter
    implements FormulationTargetFitAnalyzer {

  private static final @NonNull String REFERENCE_TYPE = "calculated-composition";
  private static final @NonNull String REFERENCE_ID = "result";

  private final @NonNull ScoreTargetFitUseCase scoreTargetFitUseCase;

  @Override
  public @NonNull FormulationTargetDistanceView evaluate(
      @NonNull Characteristics characteristics,
      @NonNull FormulationTargetDefinition target
  ) {
    final var command = new TargetFitCommand(
        new TargetFitCommand.TargetObjective(toTarget(target.sugarAcidity()), target.tannin()),
        toConfiguration(target.scoring()),
        TargetFitCommand.IntervalPolicy.MIDPOINT,
        java.util.List.of(new TargetFitCommand.Candidate(
            new TargetFitCandidateReference(REFERENCE_TYPE, REFERENCE_ID),
            new TargetFitCommand.Characteristics(
                PointCharacteristicValue.of(characteristics.specificGravity()),
                PointCharacteristicValue.of(characteristics.titratableAcidity()),
                characteristics.tannin() == null
                ? null
                : PointCharacteristicValue.of(characteristics.tannin())
            )
        ))
    );
    final Distance score;
    try {
      score = scoreTargetFitUseCase.score(command).scores().getFirst().distance();
    } catch (TargetFitTargetUnavailableException exception) {
      throw new FormulationTargetFitUnavailableException(exception.getMessage(), exception);
    }
    return toView(score);
  }

  private static TargetFitCommand.@NonNull TargetArea toTarget(
      @NonNull TargetArea target
  ) {
    return switch (target) {
      case FormulationTargetDefinition.PredefinedTarget(var zone) -> new TargetFitCommand.PredefinedTarget(
          JolicoeurSugarAcidityZoneCode.valueOf(zone.name())
      );
      case FormulationTargetDefinition.CustomRectangleTarget(var specificGravity, var titratableAcidity) ->
          new TargetFitCommand.CustomRectangleTarget(specificGravity, titratableAcidity);
    };
  }

  private static @NonNull TargetFitScoringConfiguration toConfiguration(
      @NonNull ScoringConfiguration scoring
  ) {
    return new TargetFitScoringConfiguration(
        TargetFitScoringConfiguration.Normalizer.valueOf(scoring.normalizer().name()),
        TargetFitScoringConfiguration.ObjectiveFunction.valueOf(scoring.objectiveFunction().name()),
        new TargetFitScoringConfiguration.ObjectiveWeights(
            scoring.weights().specificGravity(),
            scoring.weights().titratableAcidity(),
            scoring.weights().tannin()
        )
    );
  }

  private static @NonNull FormulationTargetDistanceView toView(
      @NonNull Distance distance
  ) {
    return new FormulationTargetDistanceView(
        FormulationTargetDistanceView.Status.valueOf(distance.status().name()),
        new FormulationTargetDistanceView.Coordinates(
            distance.point().specificGravity(),
            distance.point().titratableAcidity()
        ),
        new FormulationTargetDistanceView.Coordinates(
            distance.nearestTargetPoint().specificGravity(),
            distance.nearestTargetPoint().titratableAcidity()
        ),
        new FormulationTargetDistanceView.TargetPosition(
            FormulationTargetDistanceView.AxisPosition.valueOf(distance.targetPosition().specificGravity().name()),
            FormulationTargetDistanceView.AxisPosition.valueOf(
                distance.targetPosition().titratableAcidity().name()
            )
        ),
        distance.tannin(),
        distance.nearestTargetTannin(),
        distance.value(),
        distance.matchPercent(),
        FormulationTargetDistanceView.FitStatus.valueOf(distance.fitStatus().name()),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        FormulationTargetDefinition.ObjectiveFunction.valueOf(distance.objectiveFunction().name()),
        FormulationTargetDistanceView.Metric.valueOf(distance.metric().name()),
        new FormulationTargetDefinition.Weights(
            distance.weights().specificGravity(),
            distance.weights().titratableAcidity(),
            distance.weights().tannin()
        ),
        FormulationTargetDistanceView.TargetKind.valueOf(distance.targetKind().name()),
        FormulationTargetDistanceView.GeometryShape.valueOf(distance.targetShape().name()),
        distance.missingAxes().stream()
            .map(axis -> FormulationTargetDistanceView.EvaluationAxis.valueOf(axis.name()))
            .collect(Collectors.toUnmodifiableSet())
    );
  }
}
