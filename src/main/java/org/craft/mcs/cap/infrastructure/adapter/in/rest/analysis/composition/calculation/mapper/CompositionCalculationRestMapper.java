package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.mapper;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model.CalculatedCompositionGraphView;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculatedCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonRequestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionPlanningRestDefaults;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper.toCharacteristicsResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper.toTargetDistanceResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper.toVolumeResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper.toTargetObjective;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper.toWeights;

public final class CompositionCalculationRestMapper {

  private CompositionCalculationRestMapper() {
  }

  public static @NonNull CalculateCompositionCommand toCommand(
      @NonNull CalculateCompositionRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "calculate composition request cannot be null");
    requireNonNull(targetResolver, "sugar-acidity target resolver cannot be null");
    requireTargetForScoringConfiguration(request);

    final var tanninPolicy = requireNonNullElse(
        request.tanninPolicy(),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
    );
    final var calculator = requireNonNullElse(
        request.calculator(),
        CompositionCalculatorCode.MASS_BALANCE
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
        "composition sources cannot be null"
    ).stream()
        .map(CompositionCommonRequestMapper::toSource)
        .toList();

    return new CalculateCompositionCommand( // NOSONAR
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

  public static @NonNull CalculatedCompositionResponse toResponse(
      @NonNull CalculatedCompositionGraphView graphView
  ) {
    requireNonNull(graphView, "calculated composition graph view cannot be null");
    final var result = graphView.calculation();
    requireNonNull(result, "calculate composition result cannot be null");
    final var composition = result.composition();

    return new CalculatedCompositionResponse(
        composition.sources().stream()
            .map(CompositionCommonResponseMapper::toResponse)
            .toList(),
        toCharacteristicsResponse(composition.characteristics()),
        graphView.northAmericanTanninLevel(),
        PotentialAbvPredictionRestMapper.toResponse(result.potentialAbv()),
        toVolumeResponse(composition.totalVolume()),
        composition.warnings(),
        result.normalizerCode(),
        toTargetDistanceResponse(result.targetDistance())
    );
  }

  private static void requireTargetForScoringConfiguration(
      @NonNull CalculateCompositionRequest request
  ) {
    if (request.target() == null
        && (request.tanninTarget() != null
            || request.weights() != null
            || request.normalizer() != null
            || request.objectiveFunction() != null)) {
      throw new ApplicationValidationException(
          "composition target is required when target scoring configuration is provided"
      );
    }
  }
}
