package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.mapper;

import org.craft.mcs.cap.core.formulation.calculation.application.port.in.CalculateCompositionCommand;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTanninPolicy;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionCalculatorCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionTanninPolicy;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionWarning;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.CalculatedCompositionGraphView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculatedCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonRequestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionPlanningRestDefaults;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper.toCharacteristicsResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper.toTargetDistanceResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper.toVolumeResponse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.FormulationTargetRequestMapper.toTarget;

public final class CompositionCalculationRestMapper {

  private CompositionCalculationRestMapper() {
  }

  public static @NonNull CalculateCompositionCommand toCommand(
      @NonNull CalculateCompositionRequest request
  ) {
    requireNonNull(request, "calculate composition request cannot be null");
    requireTargetForScoringConfiguration(request);

    final var requestedTanninPolicy = request.tanninPolicy();
    final var tanninPolicy = requestedTanninPolicy == null
        ? CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
        : requestedTanninPolicy;
    final var requestedCalculator = request.calculator();
    final var calculator = requestedCalculator == null
        ? CompositionCalculatorCode.MASS_BALANCE
        : requestedCalculator;
    final var requestedNormalizer = request.normalizer();
    final var normalizer = requestedNormalizer == null
        ? CompositionPlanningRestDefaults.profileTargetNormalizer()
        : requestedNormalizer;
    final var requestedObjectiveFunction = request.objectiveFunction();
    final var objectiveFunction = requestedObjectiveFunction == null
        ? CompositionPlanningRestDefaults.profileTargetObjectiveFunction()
        : requestedObjectiveFunction;
    final var sources = requireNonNull(
        request.sources(),
        "composition sources cannot be null"
    ).stream()
        .map(CompositionCommonRequestMapper::toSourceInput)
        .toList();

    return new CalculateCompositionCommand( // NOSONAR
        sources,
        FormulationTanninPolicy.valueOf(tanninPolicy.name()),
        toTarget(request.target(), request.tanninTarget(), request.weights(), normalizer, objectiveFunction),
        FormulationCalculator.valueOf(calculator.name())
    );
  }

  public static @NonNull CalculatedCompositionResponse toResponse(
      @NonNull CalculatedCompositionGraphView graphView
  ) {
    requireNonNull(graphView, "calculated composition graph view cannot be null");
    final var result = graphView.calculation();
    requireNonNull(result, "calculate composition result cannot be null");
    final var composition = result.composition();
    final var northAmericanTanninLevel = graphView.northAmericanTanninLevel();
    final var normalizerCode = result.normalizerCode();

    return new CalculatedCompositionResponse(
        composition.sources().stream()
            .map(CompositionCommonResponseMapper::toResponse)
            .toList(),
        toCharacteristicsResponse(composition.characteristics()),
        northAmericanTanninLevel == null
            ? null
            : org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.classification.NorthAmericanTanninLevel
                .valueOf(northAmericanTanninLevel.name()),
        PotentialAbvPredictionRestMapper.toResponse(result.potentialAbv()),
        toVolumeResponse(composition.totalVolume()),
        composition.warnings().stream()
            .map(warning -> CompositionWarning.valueOf(warning.name()))
            .toList(),
        normalizerCode == null
            ? null
            : ProfileAxisNormalizerCode.valueOf(normalizerCode.name()),
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
