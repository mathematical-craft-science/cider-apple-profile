package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.mapper;

import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.ChemicalProfileCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper.toTargetObjective;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper.toTargetDistanceResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper.toVolumeResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper.toWeights;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRestMapper.toYeastAssimilableNitrogenResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper.normalizerFor;


public final class ChemicalProfileCompositionCalculationRestMapper {

  private ChemicalProfileCompositionCalculationRestMapper() {
  }

  public static @NonNull CalculateChemicalProfileCompositionCommand toCommand(
      @NonNull CalculateChemicalProfileCompositionRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "calculate chemical profile composition request cannot be null");
    requireNonNull(targetResolver, "sugar-acidity target resolver cannot be null");

    final var tanninPolicy = requireNonNullElse(
        request.tanninPolicy(),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
    );
    final ProfileTargetScoringPurpose scoringPurpose = scoringPurpose(request);
    final var calculator = requireNonNullElse(
        request.calculator(),
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE
    );
    final var normalizer = requireNonNullElse(request.normalizer(), normalizerFor(scoringPurpose));
    final var objectiveFunction = requireNonNullElse(
        request.objectiveFunction(),
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA
    );
    final var sources = requireNonNull(
        request.sources(),
        "chemical profile composition sources cannot be null"
    ).stream()
        .map(ChemicalProfileCompositionCommonRestMapper::toSource)
        .toList();

    return new CalculateChemicalProfileCompositionCommand( // NOSONAR
        sources,
        tanninPolicy,
        toTargetObjective(request.target(), request.tanninTarget(), targetResolver),
        toWeights(request.weights()),
        scoringPurpose,
        calculator,
        normalizer,
        objectiveFunction
    );
  }

  private static @NonNull ProfileTargetScoringPurpose scoringPurpose(
      @NonNull CalculateChemicalProfileCompositionRequest request
  ) {
    final var scoringPurpose = request.scoringPurpose();
    return requireNonNullElse(scoringPurpose, ProfileTargetScoringPurpose.BLENDING_PLANNING);  // NOSONAR
  }

  public static @NonNull ChemicalProfileCompositionResponse toResponse(
      @NonNull CalculateChemicalProfileCompositionResult result
  ) {
    requireNonNull(result, "calculate chemical profile composition result cannot be null");
    final var composition = result.composition();

    return new ChemicalProfileCompositionResponse(
        composition.sources().stream()
            .map(ChemicalProfileCompositionCommonRestMapper::toResponse)
            .toList(),
        SugarAcidityZoneRestMapper.toResponse(composition.profile()),
        toYeastAssimilableNitrogenResponse(composition.yeastAssimilableNitrogen()),
        toVolumeResponse(composition.totalVolume()),
        composition.warnings(),
        result.normalizerCode(),
        toTargetDistanceResponse(result.targetDistance())
    );
  }
}
