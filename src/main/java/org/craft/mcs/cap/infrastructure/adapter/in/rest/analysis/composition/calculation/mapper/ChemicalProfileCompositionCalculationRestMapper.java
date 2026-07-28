package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.mapper;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.ChemicalProfileCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRequestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonResponseMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionPlanningRestDefaults;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRequestMapper.toTargetObjective;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonRequestMapper.toWeights;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonResponseMapper.toCharacteristicsResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonResponseMapper.toTargetDistanceResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.ChemicalProfileCompositionCommonResponseMapper.toVolumeResponse;


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
    final var calculator = requireNonNullElse(
        request.calculator(),
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE
    );
    final var normalizer = requireNonNullElse(
        request.normalizer(),
        CompositionPlanningRestDefaults.profileTargetNormalizer()
    );
    final var objectiveFunction = requireNonNullElse(
        request.objectiveFunction(),
        CompositionPlanningRestDefaults.profileTargetObjectiveFunction()
    );
    final var sources = requireNonNull(
        request.sources(),
        "chemical profile composition sources cannot be null"
    ).stream()
        .map(ChemicalProfileCompositionCommonRequestMapper::toSource)
        .toList();

    return new CalculateChemicalProfileCompositionCommand( // NOSONAR
        sources,
        tanninPolicy,
        toTargetObjective(request.target(), request.tanninTarget(), targetResolver),
        new ProfileTargetScoringConfiguration(
            requireNonNull(normalizer, "profile target normalizer cannot be null"),
            requireNonNull(objectiveFunction, "profile target objective function cannot be null"),
            toWeights(request.weights())
        ),
        calculator
    );
  }

  public static @NonNull ChemicalProfileCompositionResponse toResponse(
      @NonNull CalculateChemicalProfileCompositionResult result
  ) {
    requireNonNull(result, "calculate chemical profile composition result cannot be null");
    final var composition = result.composition();

    return new ChemicalProfileCompositionResponse(
        composition.sources().stream()
            .map(ChemicalProfileCompositionCommonResponseMapper::toResponse)
            .toList(),
        toCharacteristicsResponse(composition.characteristics()),
        PotentialAbvPredictionRestMapper.toResponse(result.potentialAbv()),
        toVolumeResponse(composition.totalVolume()),
        composition.warnings(),
        result.normalizerCode(),
        toTargetDistanceResponse(result.targetDistance())
    );
  }
}
