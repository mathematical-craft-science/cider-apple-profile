package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.model.ChemicalProfileCompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRequestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonResponseMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.ChemicalProfileCompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeChemicalProfileCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionOptimizationRestMapper;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRequestMapper.toTargetObjective;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRequestMapper.toVolume;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRequestMapper.toWeights;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonResponseMapper.toCharacteristicsResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonResponseMapper.toVolumeResponse;


public final class ChemicalProfileCompositionOptimizationRestMapper {

  private ChemicalProfileCompositionOptimizationRestMapper() {
  }

  public static @NonNull OptimizeChemicalProfileCompositionCommand toCommand(
      @NonNull OptimizeChemicalProfileCompositionRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "optimize chemical profile composition request cannot be null");
    requireNonNull(targetResolver, "sugar-acidity target resolver cannot be null");

    final var tanninPolicy = requireNonNullElse(
        request.tanninPolicy(),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
    );
    final var mode = requireNonNull(request.mode(), "composition optimization mode cannot be null");
    final var algorithm = requireNonNullElse(
        request.algorithm(),
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH
    );
    final var calculator = requireNonNullElse(
        request.calculator(),
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE
    );
    final var normalizer = requireNonNullElse(
        request.normalizer(),
        CompositionOptimizationRestMapper.defaultOptimizationNormalizer()
    );
    final var objectiveFunction = requireNonNullElse(
        request.objectiveFunction(),
        CompositionOptimizationRestMapper.defaultOptimizationObjectiveFunction()
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
        "chemical profile composition optimization sources cannot be null"
    ).stream()
        .map(ChemicalProfileCompositionCommonRequestMapper::toOptimizationSource)
        .toList();

    return new OptimizeChemicalProfileCompositionCommand( // NOSONAR
        sources,
        tanninPolicy,
        targetObjective,
        new ProfileTargetScoringConfiguration(
            requireNonNull(normalizer, "profile target normalizer cannot be null"),
            requireNonNull(objectiveFunction, "profile target objective function cannot be null"),
            toWeights(request.weights())
        ),
        CompositionOptimizationRestMapper.toCriteria(request.yanReadiness()),
        settings,
        mode,
        algorithm,
        calculator,
        toVolume(request.targetTotalVolumeLiters())
    );
  }

  public static @NonNull OptimizeChemicalProfileCompositionResponse toResponse(
      @NonNull OptimizeChemicalProfileCompositionResult result,
      int stepPercent
  ) {
    requireNonNull(result, "optimize chemical profile composition result cannot be null");

    return new OptimizeChemicalProfileCompositionResponse(
        result.mode(),
        result.algorithm(),
        result.calculatorCode(),
        result.normalizerCode(),
        result.objectiveFunctionCode(),
        stepPercent,
        result.evaluatedCandidates(),
        result.candidates().stream()
            .map(ChemicalProfileCompositionOptimizationRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull ChemicalProfileCompositionOptimizationCandidateResponse toResponse(
      @NonNull ChemicalProfileCompositionOptimizationCandidateView view
  ) {
    requireNonNull(view, "chemical profile composition optimization candidate view cannot be null");
    final var candidate = view.candidate();
    final var composition = candidate.composition();

    return new ChemicalProfileCompositionOptimizationCandidateResponse(
        composition.sources().stream()
            .map(ChemicalProfileCompositionCommonResponseMapper::toResponse)
            .toList(),
        toCharacteristicsResponse(composition.characteristics()),
        PotentialAbvPredictionRestMapper.toResponse(view.potentialAbv()),
        toVolumeResponse(composition.totalVolume()),
        composition.warnings(),
        ChemicalProfileCompositionCommonResponseMapper.toResponse(candidate.distance()),
        CompositionOptimizationRestMapper.toResponse(candidate.objectiveAssessment())
    );
  }
}
