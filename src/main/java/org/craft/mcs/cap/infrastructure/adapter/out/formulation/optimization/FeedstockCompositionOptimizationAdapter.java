package org.craft.mcs.cap.infrastructure.adapter.out.formulation.optimization;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.FeedstockCompositionOptimizationResultException;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.InvalidFeedstockCompositionOptimizationException;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.UnsupportedFeedstockCompositionOptimizationException;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Criterion;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Source;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationView;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.port.out.FeedstockCompositionOptimizer;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetDistanceView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.ObjectiveFunction;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.FeedstockTargetFitUnavailableException;
import org.craft.mcs.cap.core.formulation.calculation.application.FormulationTargetFitUnavailableException;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTanninPolicy;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationMode;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationSettings;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationCriterionInput;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationObjectiveView;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationSourceInput;
import org.craft.mcs.cap.core.formulation.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizeCompositionResult;
import org.craft.mcs.cap.core.formulation.optimization.application.InvalidCompositionOptimizationRequestException;
import org.craft.mcs.cap.core.formulation.optimization.application.UnsupportedCompositionOptimizationRequestException;
import org.craft.mcs.cap.core.formulation.optimization.application.port.in.OptimizeCompositionUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** ACL from Feedstock Planning optimization language to Formulation published contracts. */
@Component
@RequiredArgsConstructor
public final class FeedstockCompositionOptimizationAdapter implements FeedstockCompositionOptimizer {

  private static final @NonNull String FEEDSTOCK_BATCH_SOURCE_TYPE = "FEEDSTOCK_BATCH";

  private final @NonNull OptimizeCompositionUseCase optimizeCompositionUseCase;

  @Override
  public @NonNull FeedstockCompositionOptimizationView optimize(
      @NonNull FeedstockCompositionOptimizationRequest request
  ) {
    try {
      return toView(optimizeCompositionUseCase.optimize(toCommand(request)));
    } catch (InvalidCompositionOptimizationRequestException exception) {
      throw new InvalidFeedstockCompositionOptimizationException(exception.getMessage(), exception);
    } catch (UnsupportedCompositionOptimizationRequestException exception) {
      throw new UnsupportedFeedstockCompositionOptimizationException(exception.getMessage(), exception);
    } catch (FormulationTargetFitUnavailableException exception) {
      throw new FeedstockTargetFitUnavailableException(exception.getMessage(), exception);
    }
  }

  private static @NonNull OptimizeCompositionCommand toCommand(
      @NonNull FeedstockCompositionOptimizationRequest request
  ) {
    return new OptimizeCompositionCommand(
        request.sources().stream().map(FeedstockCompositionOptimizationAdapter::toSource).toList(),
        FormulationTanninPolicy.valueOf(request.tanninPolicy().name()),
        toTarget(request),
        request.criteria().stream().map(FeedstockCompositionOptimizationAdapter::toCriterion).toList(),
        new CompositionOptimizationSettings(request.settings().stepPercent(), request.settings().limit()),
        CompositionOptimizationMode.valueOf(request.mode().name()),
        CompositionOptimizationAlgorithm.valueOf(request.algorithm().name()),
        FormulationCalculator.valueOf(request.calculator().name()),
        request.targetTotalVolume()
    );
  }

  private static @NonNull OptimizationSourceInput toSource(
      @NonNull Source source
  ) {
    return new OptimizationSourceInput(
        new CompositionSourceInput.Reference(
            FEEDSTOCK_BATCH_SOURCE_TYPE,
            source.batchId().value().toString()
        ),
        new CompositionSourceInput.Characteristics(
            source.characteristics().specificGravity(),
            source.characteristics().titratableAcidity(),
            source.characteristics().tannin(),
            source.characteristics().yeastAssimilableNitrogen()
        ),
        source.availableVolume()
    );
  }

  private static @NonNull OptimizationCriterionInput toCriterion(
      @NonNull Criterion criterion
  ) {
    return new OptimizationCriterionInput.YanReadiness(
        criterion.minimumYan(),
        criterion.maximumYan(),
        criterion.weight(),
        OptimizationCriterionInput.MissingPolicy.valueOf(criterion.missingPolicy().name()),
        criterion.missingPenalty()
    );
  }

  private static @NonNull FormulationTargetDefinition toTarget(
      @NonNull FeedstockCompositionOptimizationRequest request
  ) {
    final var scoring = request.scoring();
    return new FormulationTargetDefinition(
        toTargetArea(request.target()),
        request.tanninTarget(),
        new FormulationTargetDefinition.ScoringConfiguration(
            FormulationTargetDefinition.Normalizer.valueOf(scoring.normalizer().name()),
            FormulationTargetDefinition.ObjectiveFunction.valueOf(scoring.objectiveFunction().name()),
            new FormulationTargetDefinition.Weights(
                scoring.weights().specificGravity(),
                scoring.weights().titratableAcidity(),
                scoring.weights().tannin()
            )
        )
    );
  }

  private static FormulationTargetDefinition.@NonNull TargetArea toTargetArea(
      @NonNull FeedstockProfileTargetSelection target
  ) {
    return switch (target) {
      case PredefinedFeedstockProfileTargetSelection(var zone) ->
          new FormulationTargetDefinition.PredefinedTarget(FormulationTargetDefinition.Zone.valueOf(zone.name()));
      case CustomRectangleFeedstockProfileTargetSelection(var specificGravity, var titratableAcidity) ->
          new FormulationTargetDefinition.CustomRectangleTarget(specificGravity, titratableAcidity);
    };
  }

  private static @NonNull FeedstockCompositionOptimizationView toView(
      @NonNull OptimizeCompositionResult result
  ) {
    return new FeedstockCompositionOptimizationView(
        FeedstockCompositionOptimizationRequest.Mode.valueOf(result.mode().name()),
        FeedstockCompositionOptimizationRequest.Algorithm.valueOf(result.algorithm().name()),
        FeedstockCompositionOptimizationRequest.Calculator.valueOf(result.calculator().name()),
        org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration
            .Normalizer.valueOf(result.normalizer().name()),
        org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration
            .ObjectiveFunction.valueOf(result.objectiveFunction().name()),
        new FeedstockCompositionOptimizationRequest.Settings(
            result.settings().stepPercent(),
            result.settings().limit()
        ),
        result.evaluatedCandidates(),
        result.candidates().stream().map(FeedstockCompositionOptimizationAdapter::toCandidate).toList()
    );
  }

  private static FeedstockCompositionOptimizationView.@NonNull Candidate toCandidate(
      @NonNull CompositionOptimizationCandidateView candidate
  ) {
    return new FeedstockCompositionOptimizationView.Candidate(
        toComposition(candidate),
        toDistance(candidate.targetDistance()),
        toObjective(candidate.objective()),
        new FeedstockCompositionOptimizationView.PotentialAbv(
            candidate.potentialAbv().originalGravity(),
            candidate.potentialAbv().predictedFinalGravity(),
            FeedstockCompositionOptimizationView.FinalGravitySource.valueOf(
                candidate.potentialAbv().predictedFinalGravitySource().name()
            ),
            candidate.potentialAbv().potentialAbv(),
            FeedstockCompositionOptimizationView.AbvMethod.valueOf(candidate.potentialAbv().method().name()),
            FeedstockCompositionOptimizationView.Confidence.valueOf(candidate.potentialAbv().confidence().name())
        )
    );
  }

  private static FeedstockCompositionOptimizationView.@NonNull Composition toComposition(
      @NonNull CompositionOptimizationCandidateView candidate
  ) {
    final var composition = candidate.composition();
    final var characteristics = composition.characteristics();
    return new FeedstockCompositionOptimizationView.Composition(
        composition.sources().stream()
            .map(source -> new FeedstockCompositionOptimizationView.Source(
                toFeedstockBatchId(source.reference()),
                toFeedstockWeight(source.weight())
            ))
            .toList(),
        new FeedstockCompositionOptimizationView.Characteristics(
            characteristics.specificGravity(),
            characteristics.titratableAcidity(),
            characteristics.tannin(),
            characteristics.yeastAssimilableNitrogen()
        ),
        composition.totalVolume(),
        composition.warnings().stream()
            .map(warning -> FeedstockCompositionOptimizationView.Warning.valueOf(warning.name()))
            .toList()
    );
  }

  private static FeedstockCompositionOptimizationView.@NonNull Weight toFeedstockWeight(
      CompositionSourceInput.@NonNull Weight weight
  ) {
    return switch (weight) {
      case CompositionSourceInput.AbsoluteVolume(var volume) ->
          new FeedstockCompositionOptimizationView.AbsoluteVolume(volume);
      case CompositionSourceInput.Proportion(var value) ->
          new FeedstockCompositionOptimizationView.Proportion(value);
    };
  }

  private static @NonNull FeedstockBatchId toFeedstockBatchId(
      CompositionSourceInput.@NonNull Reference reference
  ) {
    if (!FEEDSTOCK_BATCH_SOURCE_TYPE.equals(reference.type())) {
      throw new FeedstockCompositionOptimizationResultException(
          "formulation result contains a non-feedstock source: " + reference.type()
      );
    }
    try {
      return FeedstockBatchId.of(reference.id());
    } catch (IllegalArgumentException exception) {
      throw new FeedstockCompositionOptimizationResultException(
          "formulation result contains an invalid feedstock batch id",
          exception
      );
    }
  }

  private static @NonNull FeedstockTargetDistanceView toDistance(
      @NonNull FormulationTargetDistanceView distance
  ) {
    return new FeedstockTargetDistanceView(
        FeedstockTargetDistanceView.Status.valueOf(distance.status().name()),
        toCoordinates(distance.point()),
        toCoordinates(distance.nearestTargetPoint()),
        distance.tannin(),
        distance.nearestTargetTannin(),
        distance.value(),
        distance.matchPercent(),
        FeedstockTargetDistanceView.FitStatus.valueOf(distance.fitStatus().name()),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        ObjectiveFunction.valueOf(distance.objectiveFunction().name()),
        FeedstockTargetDistanceView.Metric.valueOf(distance.metric().name()),
        FeedstockTargetDistanceView.TargetKind.valueOf(distance.targetKind().name()),
        FeedstockTargetDistanceView.GeometryShape.valueOf(distance.targetShape().name()),
        distance.missingAxes().stream()
            .map(axis -> FeedstockTargetDistanceView.EvaluationAxis.valueOf(axis.name()))
            .collect(Collectors.toUnmodifiableSet())
    );
  }

  private static FeedstockTargetDistanceView.@NonNull Coordinates toCoordinates(
      FormulationTargetDistanceView.@NonNull Coordinates coordinates
  ) {
    return new FeedstockTargetDistanceView.Coordinates(
        coordinates.specificGravity(),
        coordinates.titratableAcidity()
    );
  }

  private static FeedstockCompositionOptimizationView.@NonNull Objective toObjective(
      @NonNull OptimizationObjectiveView objective
  ) {
    return new FeedstockCompositionOptimizationView.Objective(
        objective.score(),
        objective.penalties().stream()
            .map(penalty -> new FeedstockCompositionOptimizationView.Penalty(
                FeedstockCompositionOptimizationView.PenaltyCode.valueOf(penalty.code().name()),
                FeedstockCompositionOptimizationView.PenaltySeverity.valueOf(penalty.severity().name()),
                penalty.value(),
                penalty.characteristic(),
                penalty.actualValue(),
                penalty.threshold()
            ))
            .toList()
    );
  }
}
