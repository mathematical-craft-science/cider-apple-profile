package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationCandidate;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.ChemicalProfileCompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeChemicalProfileCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionOptimizationRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper.toTargetObjective;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper.toVolume;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper.toVolumeResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper.toWeights;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper.toYeastAssimilableNitrogenResponse;


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
    final ProfileTargetScoringPurpose scoringPurpose = scoringPurpose(request);
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
        .map(ChemicalProfileCompositionCommonRestMapper::toOptimizationSource)
        .toList();

    return new OptimizeChemicalProfileCompositionCommand( // NOSONAR
        sources,
        tanninPolicy,
        targetObjective,
        toWeights(request.weights()),
        scoringPurpose,
        CompositionOptimizationRestMapper.toCriteria(request.yanReadiness()),
        settings,
        mode,
        algorithm,
        calculator,
        normalizer,
        objectiveFunction,
        toVolume(request.targetTotalVolumeLiters())
    );
  }

  private static @NonNull ProfileTargetScoringPurpose scoringPurpose(
      @NonNull OptimizeChemicalProfileCompositionRequest request
  ) {
    final var scoringPurpose = request.scoringPurpose();
    return requireNonNullElse(scoringPurpose, ProfileTargetScoringPurpose.BLENDING_PLANNING); // NOSONAR
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
        result.optimizationResult().evaluatedCandidates(),
        result.optimizationResult().candidates().stream()
            .map(ChemicalProfileCompositionOptimizationRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull ChemicalProfileCompositionOptimizationCandidateResponse toResponse(
      @NonNull ChemicalProfileCompositionOptimizationCandidate candidate
  ) {
    requireNonNull(candidate, "chemical profile composition optimization candidate cannot be null");
    final var composition = candidate.composition();

    return new ChemicalProfileCompositionOptimizationCandidateResponse(
        composition.sources().stream()
            .map(ChemicalProfileCompositionCommonRestMapper::toResponse)
            .toList(),
        SugarAcidityZoneRestMapper.toResponse(composition.profile()),
        toYeastAssimilableNitrogenResponse(composition.yeastAssimilableNitrogen()),
        toVolumeResponse(composition.totalVolume()),
        composition.warnings(),
        ChemicalProfileCompositionCommonRestMapper.toResponse(candidate.distance()),
        CompositionOptimizationRestMapper.toResponse(candidate.objectiveAssessment())
    );
  }
}
