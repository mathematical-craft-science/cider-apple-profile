package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionCalculatorCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionTanninPolicy;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTanninPolicy;
import org.craft.mcs.cap.core.formulation.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationMode;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationSettings;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionWarning;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.CompositionOptimizationCandidateGraphView;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.CompositionOptimizationGraphView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonRequestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.CompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionOptimizationAssessmentRestMapper;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonRequestMapper.toVolume;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper.toCharacteristicsResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper.toVolumeResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.FormulationTargetRequestMapper.toTarget;

public final class CompositionOptimizationRestMapper {

  private CompositionOptimizationRestMapper() {
  }

  public static @NonNull OptimizeCompositionCommand toCommand(
      @NonNull OptimizeCompositionRequest request
  ) {
    requireNonNull(request, "optimize composition request cannot be null");

    final var requestedTanninPolicy = request.tanninPolicy();
    final var tanninPolicy = requestedTanninPolicy == null
        ? CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
        : requestedTanninPolicy;
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
    final var defaultSettings = CompositionOptimizationSettings.defaults();
    final var requestedStepPercent = request.stepPercent();
    final var stepPercent = requestedStepPercent == null ? defaultSettings.stepPercent() : requestedStepPercent;
    final var requestedLimit = request.limit();
    final var limit = requestedLimit == null ? defaultSettings.limit() : requestedLimit;
    final var settings = new CompositionOptimizationSettings(
        stepPercent,
        limit
    );
    final var target = requireNonNull(
        toTarget(
            request.target(),
            request.tanninTarget(),
            request.weights(),
            requireNonNull(normalizer, "profile target normalizer cannot be null"),
            requireNonNull(objectiveFunction, "profile target objective function cannot be null")
        ),
        "formulation target cannot be null"
    );
    final var sources = requireNonNull(
        request.sources(),
        "composition optimization sources cannot be null"
    ).stream()
        .map(CompositionCommonRequestMapper::toOptimizationSource)
        .toList();

    return new OptimizeCompositionCommand( // NOSONAR
        sources,
        FormulationTanninPolicy.valueOf(tanninPolicy.name()),
        target,
        CompositionOptimizationAssessmentRestMapper.toCriteria(request.yanReadiness()),
        settings,
        CompositionOptimizationMode.valueOf(mode.name()),
        org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationAlgorithm
            .valueOf(algorithm.name()),
        FormulationCalculator.valueOf(calculator.name()),
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
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationMode.valueOf(
            result.mode().name()
        ),
        CompositionOptimizationAlgorithm.valueOf(result.algorithm().name()),
        CompositionCalculatorCode.valueOf(result.calculator().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode
            .valueOf(result.normalizer().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveFunctionCode
            .valueOf(result.objectiveFunction().name()),
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
    final var composition = view.composition();
    final var northAmericanTanninLevel = graphView.northAmericanTanninLevel();
    final var targetDistance = requireNonNull(
        CompositionCommonResponseMapper.toTargetDistanceResponse(view.targetDistance()),
        "optimized composition target distance response cannot be null"
    );

    return new CompositionOptimizationCandidateResponse(
        composition.sources().stream()
            .map(CompositionCommonResponseMapper::toResponse)
            .toList(),
        toCharacteristicsResponse(composition.characteristics()),
        northAmericanTanninLevel == null
            ? null
            : org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.classification.NorthAmericanTanninLevel
                .valueOf(northAmericanTanninLevel.name()),
        PotentialAbvPredictionRestMapper.toResponse(view.potentialAbv()),
        toVolumeResponse(composition.totalVolume()),
        composition.warnings().stream()
            .map(warning -> CompositionWarning.valueOf(warning.name()))
            .toList(),
        targetDistance,
        CompositionOptimizationAssessmentRestMapper.toResponse(view.objective())
    );
  }
}
