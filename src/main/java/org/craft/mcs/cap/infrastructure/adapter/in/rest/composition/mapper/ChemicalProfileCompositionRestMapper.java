package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionSourceWeight;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationCandidate;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationSource;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.CalculateChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionAxesRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionOptimizationSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionSourceReferenceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionSourceWeightRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.OptimizeChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.OptimizeChemicalProfileCompositionResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.SugarAcidityCoordinatesResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.mapper.ProfileTargetRestMapper.normalizerFor;


public final class ChemicalProfileCompositionRestMapper {

  private ChemicalProfileCompositionRestMapper() {
  }

  public static @NonNull CalculateChemicalProfileCompositionCommand toCommand(
      @NonNull CalculateChemicalProfileCompositionRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "calculate chemical profile composition request cannot be null");
    requireNonNull(targetResolver, "sugar-acidity target resolver cannot be null");

    final ChemicalProfileCompositionTanninPolicy tanninPolicy = request.tanninPolicy() == null
        ? ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
        : request.tanninPolicy();
    final ProfileTargetScoringPurpose scoringPurpose = request.scoringPurpose() == null
        ? ProfileTargetScoringPurpose.BLENDING_PLANNING
        : request.scoringPurpose();
    final ChemicalProfileCompositionCalculatorCode calculator = request.calculator() == null
                                                                ? ChemicalProfileCompositionCalculatorCode.MASS_BALANCE
                                                                : request.calculator();
    final ProfileAxisNormalizerCode normalizer = request.normalizer() == null
                                                 ? normalizerFor(scoringPurpose) // NOSONAR
                                                 : request.normalizer();
    final ProfileTargetObjectiveFunctionCode objectiveFunction = request.objectiveFunction() == null
                                                                 ? ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA
                                                                 : request.objectiveFunction();

    return new CalculateChemicalProfileCompositionCommand( // NOSONAR
        request.sources().stream()
            .map(ChemicalProfileCompositionRestMapper::toSource)
            .toList(),
        tanninPolicy,
        toTargetObjective(request.target(), request.tanninTarget(), targetResolver),
        toWeights(request.weights()),
        scoringPurpose,
        calculator,
        normalizer,
        objectiveFunction
    );
  }

  public static @NonNull ChemicalProfileCompositionResponse toResponse(
      @NonNull CalculateChemicalProfileCompositionResult result
  ) {
    requireNonNull(result, "calculate chemical profile composition result cannot be null");
    final var composition = result.composition();

    return new ChemicalProfileCompositionResponse(
        composition.sources().stream()
            .map(ChemicalProfileCompositionRestMapper::toResponse)
            .toList(),
        SugarAcidityZoneRestMapper.toResponse(composition.profile()),
        composition.yeastAssimilableNitrogen() == null
            ? null
            : SugarAcidityZoneRestMapper.toResponse(composition.yeastAssimilableNitrogen()), // NOSONAR
        composition.totalVolume() == null ? null : composition.totalVolume().liters(), // NOSONAR
        composition.warnings(),
        result.normalizerCode(),
        result.targetDistance() == null ? null : toResponse(result.targetDistance()) // NOSONAR
    );
  }

  public static @NonNull OptimizeChemicalProfileCompositionCommand toOptimizationCommand(
      @NonNull OptimizeChemicalProfileCompositionRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "optimize chemical profile composition request cannot be null");
    requireNonNull(targetResolver, "sugar-acidity target resolver cannot be null");

    final ChemicalProfileCompositionTanninPolicy tanninPolicy = request.tanninPolicy() == null
                                                                ? ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
                                                                : request.tanninPolicy();
    final ProfileTargetScoringPurpose scoringPurpose = request.scoringPurpose() == null
                                                       ? ProfileTargetScoringPurpose.BLENDING_PLANNING
                                                       : request.scoringPurpose();
    final var mode = requireNonNull(request.mode(), "composition optimization mode cannot be null");
    final CompositionOptimizationAlgorithm algorithm = request.algorithm() == null
                                                       ? CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH
                                                       : request.algorithm();
    final ChemicalProfileCompositionCalculatorCode calculator = request.calculator() == null
                                                                ? ChemicalProfileCompositionCalculatorCode.MASS_BALANCE
                                                                : request.calculator();
    final ProfileAxisNormalizerCode normalizer = request.normalizer() == null
                                                 ? normalizerFor(scoringPurpose)
                                                 : request.normalizer();
    final ProfileTargetObjectiveFunctionCode objectiveFunction = request.objectiveFunction() == null
                                                                 ? ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA
                                                                 : request.objectiveFunction();
    final var settings = new DiscretizedCompositionOptimizationSettings(
        request.stepPercent() == null
        ? DiscretizedCompositionOptimizationSettings.DEFAULT_STEP_PERCENT
        : request.stepPercent(),
        request.limit() == null
        ? DiscretizedCompositionOptimizationSettings.DEFAULT_LIMIT
        : request.limit()
    );
    final var targetObjective = requireNonNull(
        toTargetObjective(request.target(), request.tanninTarget(), targetResolver),
        "profile target objective cannot be null"
    );

    return new OptimizeChemicalProfileCompositionCommand( // NOSONAR
        request.sources().stream()
            .map(ChemicalProfileCompositionRestMapper::toOptimizationSource)
            .toList(),
        tanninPolicy,
        targetObjective,
        toWeights(request.weights()),
        scoringPurpose,
        settings,
        mode,
        algorithm,
        calculator,
        normalizer,
        objectiveFunction,
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
        result.optimizationResult().evaluatedCandidates(),
        result.optimizationResult().candidates().stream()
            .map(ChemicalProfileCompositionRestMapper::toOptimizationCandidateResponse)
            .toList()
    );
  }

  private static @NonNull ChemicalProfileCompositionSourceResponse toResponse(
      @NonNull ChemicalProfileCompositionSource source
  ) {
    requireNonNull(source, "chemical profile composition source cannot be null");

    return new ChemicalProfileCompositionSourceResponse(
        toResponse(source.reference()),
        requestedVolume(source),
        proportion(source)
    );
  }

  private static @Nullable BigDecimal requestedVolume(@NonNull ChemicalProfileCompositionSource source) {
    return source.weight() instanceof AbsoluteCompositionVolume(VolumeValue volume) ? volume.liters() : null;
  }

  private static @Nullable BigDecimal proportion(@NonNull ChemicalProfileCompositionSource source) {
    return source.weight() instanceof CompositionProportion(BigDecimal proportion) ? proportion : null;
  }

  private static @NonNull ChemicalProfileCompositionSource toSource(
      @NonNull ChemicalProfileCompositionSourceRequest request
  ) {
    requireNonNull(request, "chemical profile composition calculation source request cannot be null");

    return new ChemicalProfileCompositionSource(
        toReference(request.reference()),
        toAxes(request.axes()),
        toWeight(request.weight()),
        toVolume(request.availableVolumeLiters())
    );
  }

  private static @NonNull ChemicalProfileCompositionOptimizationSource toOptimizationSource(
      @NonNull ChemicalProfileCompositionOptimizationSourceRequest request
  ) {
    requireNonNull(request, "chemical profile composition optimization source request cannot be null");

    return new ChemicalProfileCompositionOptimizationSource(
        toReference(request.reference()),
        toAxes(request.axes()),
        toVolume(request.availableVolumeLiters())
    );
  }

  private static @NonNull ChemicalProfileCompositionOptimizationCandidateResponse toOptimizationCandidateResponse(
      @NonNull ChemicalProfileCompositionOptimizationCandidate candidate
  ) {
    requireNonNull(candidate, "chemical profile composition optimization candidate cannot be null");
    final var composition = candidate.composition();

    return new ChemicalProfileCompositionOptimizationCandidateResponse(
        composition.sources().stream()
            .map(ChemicalProfileCompositionRestMapper::toResponse)
            .toList(),
        SugarAcidityZoneRestMapper.toResponse(composition.profile()),
        composition.yeastAssimilableNitrogen() == null
            ? null
            : SugarAcidityZoneRestMapper.toResponse(composition.yeastAssimilableNitrogen()), // NOSONAR
        composition.totalVolume() == null ? null : composition.totalVolume().liters(), // NOSONAR
        composition.warnings(),
        toResponse(candidate.distance())
    );
  }

  private static @NonNull ChemicalProfileCompositionSourceReference toReference(
      @NonNull ChemicalProfileCompositionSourceReferenceRequest request
  ) {
    requireNonNull(request, "Chemical profile composition source reference request cannot be null");
    return new ChemicalProfileCompositionSourceReference(request.type(), request.id());
  }

  private static @NonNull ChemicalProfileCompositionSourceReferenceRequest toResponse(
      @NonNull ChemicalProfileCompositionSourceReference reference
  ) {
    requireNonNull(reference, "Chemical profile composition source reference cannot be null");
    return new ChemicalProfileCompositionSourceReferenceRequest(reference.type(), reference.id());
  }

  private static @NonNull ChemicalProfileAxes toAxes(@NonNull ChemicalProfileCompositionAxesRequest request) {
    requireNonNull(request, "chemical profile composition source profile axes request cannot be null");

    return ChemicalProfileAxes.of(
        axis(ChemicalAxis.SUGAR, request.sugar()),
        axis(ChemicalAxis.ACIDITY, request.acidity()),
        request.tannin() == null ? null : axis(ChemicalAxis.TANNINITY, request.tannin()), // NOSONAR
        request.yeastAssimilableNitrogen() == null
            ? null
            : axis(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN, request.yeastAssimilableNitrogen()) // NOSONAR
    );
  }

  private static @NonNull ChemicalProfileCompositionSourceWeight toWeight(
      @NonNull ChemicalProfileCompositionSourceWeightRequest request
  ) {
    requireNonNull(request, "chemical profile composition source weight request cannot be null");
    requireNonNull(request.kind(), "chemical profile composition source weight kind cannot be null");

    return switch (request.kind()) {
      case ABSOLUTE_VOLUME_LITERS -> new AbsoluteCompositionVolume(VolumeValue.ofLiters(request.value()));
      case PROPORTION -> new CompositionProportion(request.value());
    };
  }

  private static @Nullable VolumeValue toVolume(@Nullable BigDecimal liters) {
    return liters == null ? null : VolumeValue.ofLiters(liters);
  }

  private static @Nullable ProfileTargetObjective toTargetObjective(
      @Nullable ProfileTargetRequest request,
      @Nullable ProfileTargetTanninRangeRequest tanninTarget,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    if (request == null) {
      return null;
    }

    final var sugarAcidityTarget = toTarget(request, targetResolver);
    return tanninTarget == null
           ? ProfileTargetObjective.of(sugarAcidityTarget)
           : ProfileTargetObjective.withTanninTarget(sugarAcidityTarget, toTanninTarget(tanninTarget)); // NOSONAR
  }

  private static @NonNull SugarAcidityTarget toTarget(
      @NonNull ProfileTargetRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "profile target request cannot be null");
    return switch (request) {
      case PredefinedZoneProfileTargetRequest predefined -> targetResolver.resolvePredefinedZone(predefined.zone());
      case CustomRectangleProfileTargetRequest custom -> new CustomRectangleSugarAcidityTarget(
          new RectangleSugarAcidityTargetGeometry(
              custom.sugarMin(),
              custom.sugarMax(),
              custom.acidityMin(),
              custom.acidityMax()
          )
      );
    };
  }

  private static @NonNull TanninTargetRange toTanninTarget(
      @NonNull ProfileTargetTanninRangeRequest request
  ) {
    requireNonNull(request, "profile target tannin range request cannot be null");
    return new TanninTargetRange(request.min(), request.max());
  }

  private static @NonNull ProfileTargetObjectiveWeights toWeights(
      @Nullable ProfileTargetObjectiveWeightsRequest request
  ) {
    if (request == null) {
      return ProfileTargetObjectiveWeights.balanced();
    }

    final BigDecimal sugar = request.sugar() == null ? BigDecimal.ONE : request.sugar();
    final BigDecimal acidity = request.acidity() == null ? BigDecimal.ONE : request.acidity();
    return new ProfileTargetObjectiveWeights(sugar, acidity, request.tannin()); // NOSONAR
  }

  private static @NonNull ProfileTargetDistanceResponse toResponse(
      @NonNull ProfileTargetDistance distance
  ) {
    requireNonNull(distance, "profile target distance cannot be null");

    return new ProfileTargetDistanceResponse(
        distance.status(),
        distance.value(),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        toResponse(distance.nearestTargetPoint()),
        distance.tannin(),
        distance.nearestTargetTannin(),
        distance.objectiveFunction(),
        distance.metric(),
        distance.targetKind(),
        distance.targetShape(),
        distance.missingMeasurements()
    );
  }

  private static @NonNull SugarAcidityCoordinatesResponse toResponse(
      @NonNull SugarAcidityCoordinates coordinates
  ) {
    requireNonNull(coordinates, "sugar-acidity coordinates cannot be null");
    return new SugarAcidityCoordinatesResponse(
        coordinates.sugar(),
        coordinates.acidity()
    );
  }

  private static @NonNull AxisValue axis(@NonNull ChemicalAxis axis, @NonNull BigDecimal value) {
    requireNonNull(axis, "chemical axis cannot be null");
    requireNonNull(value, "chemical axis value cannot be null");

    return new AxisValue(
        axis,
        value,
        MeasurementValueKind.EXACT,
        null,
        null,
        null,
        null
    );
  }
}
