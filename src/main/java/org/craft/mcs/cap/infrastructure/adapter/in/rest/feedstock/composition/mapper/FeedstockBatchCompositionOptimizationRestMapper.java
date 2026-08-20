package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningProfileFilters;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.port.in.OptimizeFeedstockBatchCompositionResult;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionCalculatorCode;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Criterion;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.ObjectiveWeights;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionWarning;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchCompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchCompositionOptimizationSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.OptimizeFeedstockBatchCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.OptimizeFeedstockBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionOptimizationAssessmentRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationYanReadinessRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper
    .toPlanningBatchCriteria;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper
    .toSeason;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphCriteriaRestMapper
    .toFeedstockPlanningGraphCriteria;


/** Maps the Feedstock composition-optimization transport contract. */
public final class FeedstockBatchCompositionOptimizationRestMapper {

  private FeedstockBatchCompositionOptimizationRestMapper() {
  }

  public static @NonNull OptimizeFeedstockBatchCompositionCommand toCommand(
      @NonNull OptimizeFeedstockBatchCompositionRequest request
  ) {
    requireNonNull(request, "optimize feedstock batch composition request cannot be null");
    final var target = requireNonNull(request.target(), "feedstock batch profile target cannot be null");
    final var defaultSettings = FeedstockCompositionOptimizationRequest.Settings.defaults();
    final var requestedStepPercent = request.stepPercent();
    final var stepPercent = requestedStepPercent == null ? defaultSettings.stepPercent() : requestedStepPercent;
    final var requestedLimit = request.limit();
    final var limit = requestedLimit == null ? defaultSettings.limit() : requestedLimit;
    final var settings = new FeedstockCompositionOptimizationRequest.Settings(
        stepPercent,
        limit
    );
    final var mode = requireNonNull(request.mode(), "composition optimization mode cannot be null");
    final var requestedAlgorithm = request.algorithm();
    final var algorithm = requestedAlgorithm == null
        ? CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH
        : requestedAlgorithm;
    final var requestedCalculator = request.calculator();
    final var calculator = requestedCalculator == null
        ? CompositionCalculatorCode.MASS_BALANCE
        : requestedCalculator;
    final var requestedNormalizer = request.normalizer();
    final var normalizer = requestedNormalizer == null
        ? CompositionOptimizationAssessmentRestMapper.defaultOptimizationNormalizer()
        : requestedNormalizer;
    final var requestedObjectiveFunction = request.objectiveFunction();
    final var objectiveFunction = requestedObjectiveFunction == null
        ? CompositionOptimizationAssessmentRestMapper.defaultOptimizationObjectiveFunction()
        : requestedObjectiveFunction;
    final var requestedSourceLimit = request.sourceLimit();
    final var sourceLimit = requestedSourceLimit == null ? QueryLimits.MAXIMUM : requestedSourceLimit;

    return new OptimizeFeedstockBatchCompositionCommand( // NOSONAR
        WorkspaceId.of(request.workspaceId()),
        new FeedstockPlanningProfileFilters(
            toPlanningBatchCriteria(request.batchIds()),
            toSeason(request.season())
        ),
        toFeedstockPlanningGraphCriteria(request.zone(), null),
        FeedstockProfileTargetRestMapper.toSelection(target),
        FeedstockProfileTargetRestMapper.toTanninTarget(request.tanninTarget()),
        new FeedstockCompositionOptimizationRequest.Scoring(
            FeedstockTargetFitConfiguration.Normalizer.valueOf(
                normalizer.name()
            ),
            FeedstockTargetFitConfiguration.ObjectiveFunction.valueOf(
                objectiveFunction.name()
            ),
            toWeights(request.weights())
        ),
        toCriteria(request.yanReadiness()),
        settings,
        FeedstockCompositionOptimizationRequest.Mode.valueOf(mode.name()),
        FeedstockCompositionOptimizationRequest.Algorithm.valueOf(algorithm.name()),
        FeedstockCompositionOptimizationRequest.Calculator.valueOf(calculator.name()),
        toVolume(request.targetTotalVolumeLiters()),
        sourceLimit
    );
  }

  public static @NonNull OptimizeFeedstockBatchCompositionResponse toResponse(
      @NonNull OptimizeFeedstockBatchCompositionResult result
  ) {
    requireNonNull(result, "optimize feedstock batch composition result cannot be null");
    final var optimization = result.optimization();

    return new OptimizeFeedstockBatchCompositionResponse(
        result.sourceCount(),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationMode
            .valueOf(optimization.mode().name()),
        CompositionOptimizationAlgorithm.valueOf(optimization.algorithm().name()),
        CompositionCalculatorCode.valueOf(optimization.calculator().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode
            .valueOf(optimization.normalizer().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveFunctionCode
            .valueOf(optimization.objectiveFunction().name()),
        optimization.settings().stepPercent(),
        optimization.evaluatedCandidates(),
        optimization.candidates().stream()
            .map(FeedstockBatchCompositionOptimizationRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockBatchCompositionOptimizationCandidateResponse toResponse(
      FeedstockCompositionOptimizationView.@NonNull Candidate view
  ) {
    requireNonNull(view, "composition optimization candidate view cannot be null");
    final var composition = view.composition();

    return new FeedstockBatchCompositionOptimizationCandidateResponse(
        composition.sources().stream()
            .map(FeedstockBatchCompositionOptimizationRestMapper::toSourceResponse)
            .toList(),
        CompositionCommonResponseMapper.toCharacteristicsResponse(
            composition.characteristics()
        ),
        toVolumeResponse(composition.totalVolume()),
        composition.warnings().stream()
            .map(warning -> CompositionWarning.valueOf(warning.name()))
            .toList(),
        ProfileTargetRestMapper.toResponse(view.targetDistance()),
        CompositionOptimizationAssessmentRestMapper.toResponse(view.objective())
    );
  }

  private static @NonNull FeedstockBatchCompositionOptimizationSourceResponse toSourceResponse(
      FeedstockCompositionOptimizationView.@NonNull Source source
  ) {
    requireNonNull(source, "composition source cannot be null");
    return new FeedstockBatchCompositionOptimizationSourceResponse(
        source.batchId().value(),
        toRequestedVolumeResponse(source.requestedVolume()),
        source.proportion()
    );
  }

  private static @Nullable DecimalQuantityResponse toVolumeResponse(@Nullable VolumeValue volume) {
    return volume == null ? null : new DecimalQuantityResponse(volume.liters(), CanonicalUnitLabels.VOLUME);
  }

  private static @Nullable DecimalQuantityResponse toRequestedVolumeResponse(@Nullable VolumeValue volume) {
    return volume == null ? null : new DecimalQuantityResponse(volume.liters(), CanonicalUnitLabels.VOLUME);
  }

  private static @Nullable VolumeValue toVolume(@Nullable BigDecimal liters) {
    return liters == null ? null : VolumeValue.ofLiters(liters);
  }

  private static @NonNull ObjectiveWeights toWeights(
      @Nullable ProfileTargetObjectiveWeightsRequest weights
  ) {
    final var specificGravity = weights == null ? null : weights.specificGravity();
    final var titratableAcidity = weights == null ? null : weights.titratableAcidity();
    final var tannin = weights == null ? null : weights.tannin();
    return new FeedstockTargetFitConfiguration.ObjectiveWeights(
        specificGravity == null ? BigDecimal.ONE : specificGravity,
        titratableAcidity == null ? BigDecimal.ONE : titratableAcidity,
        tannin
    );
  }

  private static @NonNull List<@NonNull Criterion> toCriteria(
      @Nullable CompositionOptimizationYanReadinessRequest request
  ) {
    if (request == null) {
      return List.of();
    }
    final var criteria = CompositionOptimizationAssessmentRestMapper.toCriteria(request);
    return criteria.stream()
        .map(criterion -> switch (criterion) {
          case org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationCriterionInput
              .YanReadiness(var minimum, var maximum, var weight, var missingPolicy, var missingPenalty) ->
              new FeedstockCompositionOptimizationRequest.Criterion(
                  minimum,
                  maximum,
                  weight,
                  FeedstockCompositionOptimizationRequest.MissingPolicy.valueOf(missingPolicy.name()),
                  missingPenalty
              );
        })
        .toList();
  }
}
