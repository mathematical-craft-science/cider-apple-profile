package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationCandidate;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.application.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.OptimizeFeedstockBatchCompositionResult;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCompositionOptimizationSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchProfileTargetScoreBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchProfileTargetScoreResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.OptimizeFeedstockBatchCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.OptimizeFeedstockBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.ScoreFeedstockBatchProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.SugarAcidityCoordinatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.mapper.ProfileTargetRestMapper.normalizerFor;


public final class FeedstockBatchProfileTargetScoreRestMapper {

  private FeedstockBatchProfileTargetScoreRestMapper() {
  }

  public static @NonNull ScoreFeedstockBatchProfileTargetCommand toCommand(
      @NonNull ScoreFeedstockBatchProfileTargetRequest request
  ) {
    requireNonNull(request, "score feedstock batch profile target request cannot be null");

    return new ScoreFeedstockBatchProfileTargetCommand(
        CiderMakerId.of(request.ciderMakerId()),
        new FeedstockMeasurementPointFilters(
            request.batchId() == null ? null : FeedstockBatchCriteria.of(FeedstockBatchId.of(request.batchId())), // NOSONAR
            toSeason(request.season()),
            request.zone() == null ? null : SugarAcidityZoneCriteria.of(request.zone()), // NOSONAR
            request.capability() == null
                ? null
                : new ChemicalProfileCapabilityCriteria(request.capability(), request.capable()) // NOSONAR
        ),
        request.purpose(),
        toTarget(request.target()),
        toTanninTarget(request.tanninTarget()),
        toWeights(request.weights()),
        request.limit() == null ? CursorPageRequest.DEFAULT_LIMIT : request.limit()
    );
  }

  public static @NonNull FeedstockBatchProfileTargetScoreBatchResponse toResponse(
      @NonNull FeedstockBatchProfileTargetScores scores
  ) {
    requireNonNull(scores, "feedstock batch profile target scores cannot be null");

    return new FeedstockBatchProfileTargetScoreBatchResponse(
        scores.purpose(),
        scores.normalizerCode(),
        scores.scores().stream()
            .map(FeedstockBatchProfileTargetScoreRestMapper::toResponse)
            .toList()
    );
  }

  public static @NonNull OptimizeFeedstockBatchCompositionCommand toOptimizationCommand(
      @NonNull OptimizeFeedstockBatchCompositionRequest request
  ) {
    requireNonNull(request, "optimize feedstock batch composition request cannot be null");
    final var purpose = request.purpose() == null
        ? ProfileTargetScoringPurpose.BLENDING_PLANNING
        : request.purpose();
    final var settings = new DiscretizedCompositionOptimizationSettings(
        request.stepPercent() == null
            ? DiscretizedCompositionOptimizationSettings.DEFAULT_STEP_PERCENT
            : request.stepPercent(),
        request.limit() == null
            ? DiscretizedCompositionOptimizationSettings.DEFAULT_LIMIT
            : request.limit()
    );

    return new OptimizeFeedstockBatchCompositionCommand( // NOSONAR
        CiderMakerId.of(request.ciderMakerId()),
        new FeedstockMeasurementPointFilters(
            toBatchCriteria(request.batchIds()),
            toSeason(request.season()),
            request.zone() == null ? null : SugarAcidityZoneCriteria.of(request.zone()), // NOSONAR
            request.capability() == null
                ? null
                : new ChemicalProfileCapabilityCriteria(request.capability(), request.capable()) // NOSONAR
        ),
        purpose,
        toTarget(request.target()),
        toTanninTarget(request.tanninTarget()),
        toWeights(request.weights()),
        settings,
        request.mode(),
        request.algorithm() == null
            ? CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH
            : request.algorithm(),
        request.calculator() == null
            ? ChemicalProfileCompositionCalculatorCode.MASS_BALANCE
            : request.calculator(),
        request.normalizer() == null
            ? normalizerFor(purpose)
            : request.normalizer(),
        request.objectiveFunction() == null
            ? ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA
            : request.objectiveFunction(),
        request.targetTotalVolumeLiters() == null ? null : VolumeValue.ofLiters(request.targetTotalVolumeLiters()), // NOSONAR
        request.sourceLimit() == null ? CursorPageRequest.MAX_LIMIT : request.sourceLimit()
    );
  }

  public static @NonNull OptimizeFeedstockBatchCompositionResponse toResponse(
      @NonNull OptimizeFeedstockBatchCompositionResult result,
      int stepPercent
  ) {
    requireNonNull(result, "optimize feedstock batch composition result cannot be null");
    final var optimization = result.optimization();

    return new OptimizeFeedstockBatchCompositionResponse(
        result.sourceCount(),
        optimization.mode(),
        optimization.algorithm(),
        optimization.calculatorCode(),
        optimization.normalizerCode(),
        optimization.objectiveFunctionCode(),
        stepPercent,
        optimization.optimizationResult().evaluatedCandidates(),
        optimization.optimizationResult().candidates().stream()
            .map(FeedstockBatchProfileTargetScoreRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockBatchProfileTargetScoreResponse toResponse(
      @NonNull FeedstockBatchProfileTargetScore score
  ) {
    requireNonNull(score, "feedstock batch profile target score cannot be null");

    return new FeedstockBatchProfileTargetScoreResponse(
        score.batchId().value(),
        score.ciderMakerId().value(),
        score.currentMeasurementId().value(),
        score.currentMeasurementName().value(),
        SugarAcidityZoneRestMapper.toResponse(score.point()),
        toResponse(score.distance())
    );
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
        quantity(distance.tannin(), CanonicalUnitLabels.TANNINITY),
        quantity(distance.nearestTargetTannin(), CanonicalUnitLabels.TANNINITY),
        distance.objectiveFunction(),
        distance.metric(),
        distance.targetKind(),
        distance.targetShape(),
        distance.missingMeasurements()
    );
  }

  private static @NonNull FeedstockBatchCompositionOptimizationCandidateResponse toResponse(
      @NonNull ChemicalProfileCompositionOptimizationCandidate candidate
  ) {
    requireNonNull(candidate, "chemical profile composition optimization candidate cannot be null");
    final var composition = candidate.composition();

    return new FeedstockBatchCompositionOptimizationCandidateResponse(
        composition.sources().stream()
            .map(FeedstockBatchProfileTargetScoreRestMapper::toSourceResponse)
            .toList(),
        SugarAcidityZoneRestMapper.toResponse(composition.profile()),
        composition.yeastAssimilableNitrogen() == null
            ? null
            : SugarAcidityZoneRestMapper.toResponse( // NOSONAR
                composition.yeastAssimilableNitrogen(),
                CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
            ),
        composition.totalVolume() == null
            ? null
            : new DecimalQuantityResponse(composition.totalVolume().liters(), CanonicalUnitLabels.VOLUME), // NOSONAR
        List.copyOf(composition.warnings()),
        toResponse(candidate.distance())
    );
  }

  private static @NonNull FeedstockBatchCompositionOptimizationSourceResponse toSourceResponse(
      @NonNull ChemicalProfileCompositionSource source
  ) {
    requireNonNull(source, "chemical profile composition source cannot be null");
    final var requestedVolume = requestedVolume(source);
    return new FeedstockBatchCompositionOptimizationSourceResponse(
        UUID.fromString(source.reference().id()),
        requestedVolume == null ? null : new DecimalQuantityResponse(requestedVolume, CanonicalUnitLabels.VOLUME), // NOSONAR
        proportion(source)
    );
  }

  private static @Nullable BigDecimal requestedVolume(@NonNull ChemicalProfileCompositionSource source) {
    return source.weight() instanceof AbsoluteCompositionVolume(VolumeValue volume) ? volume.liters() : null;
  }

  private static @Nullable BigDecimal proportion(@NonNull ChemicalProfileCompositionSource source) {
    return source.weight() instanceof CompositionProportion(BigDecimal value) ? value : null;
  }

  private static @NonNull SugarAcidityCoordinatesResponse toResponse(
      @NonNull SugarAcidityCoordinates coordinates
  ) {
    requireNonNull(coordinates, "sugar-acidity coordinates cannot be null");
    return new SugarAcidityCoordinatesResponse(
        new DecimalQuantityResponse(coordinates.sugar(), CanonicalUnitLabels.SUGAR),
        new DecimalQuantityResponse(coordinates.acidity(), CanonicalUnitLabels.TITRATABLE_ACIDITY)
    );
  }

  private static @Nullable DecimalQuantityResponse quantity(@Nullable BigDecimal value, @NonNull String unit) {
    return value == null ? null : new DecimalQuantityResponse(value, unit);
  }

  private static @NonNull FeedstockProfileTargetSelection toTarget(
      @NonNull ProfileTargetRequest request
  ) {
    requireNonNull(request, "feedstock batch profile target request cannot be null");
    return switch (request) {
      case PredefinedZoneProfileTargetRequest predefined ->
          new PredefinedFeedstockProfileTargetSelection(predefined.zone());
      case CustomRectangleProfileTargetRequest custom ->
          new CustomRectangleFeedstockProfileTargetSelection(new RectangleSugarAcidityTargetGeometry(
              custom.sugarMin(),
              custom.sugarMax(),
              custom.acidityMin(),
              custom.acidityMax()
          ));
    };
  }

  private static @Nullable FeedstockBatchCriteria toBatchCriteria(
      @Nullable List<@NonNull UUID> batchIds
  ) {
    if (batchIds == null || batchIds.isEmpty()) {
      return null;
    }
    return new FeedstockBatchCriteria(
        batchIds.stream()
            .map(FeedstockBatchId::of)
            .collect(Collectors.toSet())
    );
  }

  private static @Nullable TanninTargetRange toTanninTarget(
      @Nullable ProfileTargetTanninRangeRequest request
  ) {
    return request == null ? null : new TanninTargetRange(request.min(), request.max());
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

  private static @Nullable FeedstockSeason toSeason(@Nullable FeedstockSeasonRequest request) {
    return request == null ? null : FeedstockSeason.of(request.startsOn(), request.endsOn());
  }

}
