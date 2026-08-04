package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model.CompositionOptimizationCandidateGraphView;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model.CompositionOptimizationGraphView;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonRequestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.CompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionOptimizationAssessmentRestMapper;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonRequestMapper.toVolume;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper.toCharacteristicsResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper.toVolumeResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper.toTargetObjective;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper.toWeights;

public final class CompositionOptimizationRestMapper {

  private CompositionOptimizationRestMapper() {
  }

  public static @NonNull OptimizeCompositionCommand toCommand(
      @NonNull OptimizeCompositionRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "optimize composition request cannot be null");
    requireNonNull(targetResolver, "sugar-acidity target resolver cannot be null");

    final var tanninPolicy = requireNonNullElse(
        request.tanninPolicy(),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
    );
    final var mode = requireNonNull(request.mode(), "composition optimization mode cannot be null");
    final var algorithm = requireNonNullElse(
        request.algorithm(),
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH
    );
    final var calculator = requireNonNullElse(
        request.calculator(),
        CompositionCalculatorCode.MASS_BALANCE
    );
    final var normalizer = requireNonNullElse(
        request.normalizer(),
        CompositionOptimizationAssessmentRestMapper.defaultOptimizationNormalizer()
    );
    final var objectiveFunction = requireNonNullElse(
        request.objectiveFunction(),
        CompositionOptimizationAssessmentRestMapper.defaultOptimizationObjectiveFunction()
    );
    final var settings = new DiscretizedCompositionOptimizationSettings(
        requireNonNullElse(request.stepPercent(), DiscretizedCompositionOptimizationSettings.DEFAULT_STEP_PERCENT),
        requireNonNullElse(request.limit(), DiscretizedCompositionOptimizationSettings.DEFAULT_LIMIT)
    );
    final var targetObjective = requireNonNull(
        toTargetObjective(request.target(), request.tanninTarget(), targetResolver),
        "profile target objective cannot be null"
    );
    final var sources = requireNonNull(
        request.sources(),
        "composition optimization sources cannot be null"
    ).stream()
        .map(CompositionCommonRequestMapper::toOptimizationSource)
        .toList();

    return new OptimizeCompositionCommand( // NOSONAR
        sources,
        tanninPolicy,
        targetObjective,
        new ProfileTargetScoringConfiguration(
            requireNonNull(normalizer, "profile target normalizer cannot be null"),
            requireNonNull(objectiveFunction, "profile target objective function cannot be null"),
            toWeights(request.weights())
        ),
        CompositionOptimizationAssessmentRestMapper.toCriteria(request.yanReadiness()),
        settings,
        mode,
        algorithm,
        calculator,
        toVolume(request.targetTotalVolumeLiters())
    );
  }

  public static @NonNull OptimizeCompositionResponse toResponse(
      @NonNull CompositionOptimizationGraphView graphView
  ) {
    requireNonNull(graphView, "composition optimization graph view cannot be null");
    final var result = graphView.optimization();
    requireNonNull(result, "optimize composition result cannot be null");

    return new OptimizeCompositionResponse(
        result.mode(),
        result.algorithm(),
        result.calculatorCode(),
        result.normalizerCode(),
        result.objectiveFunctionCode(),
        result.settings().stepPercent(),
        result.evaluatedCandidates(),
        graphView.candidates().stream()
            .map(CompositionOptimizationRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull CompositionOptimizationCandidateResponse toResponse(
      @NonNull CompositionOptimizationCandidateGraphView graphView
  ) {
    requireNonNull(graphView, "composition optimization candidate graph view cannot be null");
    final var view = graphView.candidate();
    final var candidate = view.candidate();
    final var composition = candidate.composition();

    return new CompositionOptimizationCandidateResponse(
        composition.sources().stream()
            .map(CompositionCommonResponseMapper::toResponse)
            .toList(),
        toCharacteristicsResponse(composition.characteristics()),
        graphView.northAmericanTanninLevel(),
        PotentialAbvPredictionRestMapper.toResponse(view.potentialAbv()),
        toVolumeResponse(composition.totalVolume()),
        composition.warnings(),
        CompositionCommonResponseMapper.toResponse(candidate.distance()),
        CompositionOptimizationAssessmentRestMapper.toResponse(candidate.objectiveAssessment())
    );
  }
}
