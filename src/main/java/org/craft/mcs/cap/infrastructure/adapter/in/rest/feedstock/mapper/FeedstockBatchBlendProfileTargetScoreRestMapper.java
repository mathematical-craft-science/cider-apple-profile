package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.CompositionProportion;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.ChemicalProfileCompositionOptimizationCandidate;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.application.port.in.CustomRectangleFeedstockBatchBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchBlendProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchBlendProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.OptimizeFeedstockBatchCompositionResult;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockBatchBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchBlendProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCompositionOptimizationSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.OptimizeFeedstockBatchCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.OptimizeFeedstockBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.ProfileTargetDistanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CustomRectangleFeedstockBatchBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchBlendProfileTargetScoreBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchBlendProfileTargetScoreResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.PredefinedZoneFeedstockBatchBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.ScoreFeedstockBatchBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.SugarAcidityCoordinatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.TanninTargetRangeRequest;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class FeedstockBatchBlendProfileTargetScoreRestMapper {

  private FeedstockBatchBlendProfileTargetScoreRestMapper() {
  }

  public static @NonNull ScoreFeedstockBatchBlendProfileTargetCommand toCommand(
      @NonNull ScoreFeedstockBatchBlendProfileTargetRequest request
  ) {
    requireNonNull(request, "score feedstock batch blend profile target request cannot be null");

    return new ScoreFeedstockBatchBlendProfileTargetCommand(
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

  public static @NonNull FeedstockBatchBlendProfileTargetScoreBatchResponse toResponse(
      @NonNull FeedstockBatchBlendProfileTargetScores scores
  ) {
    requireNonNull(scores, "feedstock batch blend profile target scores cannot be null");

    return new FeedstockBatchBlendProfileTargetScoreBatchResponse(
        scores.purpose(),
        scores.normalizerCode(),
        scores.scores().stream()
            .map(FeedstockBatchBlendProfileTargetScoreRestMapper::toResponse)
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
            .map(FeedstockBatchBlendProfileTargetScoreRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockBatchBlendProfileTargetScoreResponse toResponse(
      @NonNull FeedstockBatchBlendProfileTargetScore score
  ) {
    requireNonNull(score, "feedstock batch blend profile target score cannot be null");

    return new FeedstockBatchBlendProfileTargetScoreResponse(
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
        distance.tannin(),
        distance.nearestTargetTannin(),
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
            .map(FeedstockBatchBlendProfileTargetScoreRestMapper::toSourceResponse)
            .toList(),
        SugarAcidityZoneRestMapper.toResponse(composition.profile()),
        composition.totalVolume() == null ? null : composition.totalVolume().liters(), // NOSONAR
        List.copyOf(composition.warnings()),
        toResponse(candidate.distance())
    );
  }

  private static @NonNull FeedstockBatchCompositionOptimizationSourceResponse toSourceResponse(
      @NonNull ChemicalProfileCompositionSource source
  ) {
    requireNonNull(source, "chemical profile composition source cannot be null");
    return new FeedstockBatchCompositionOptimizationSourceResponse(
        UUID.fromString(source.reference().id()),
        requestedVolume(source),
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
        coordinates.sugar(),
        coordinates.acidity()
    );
  }

  private static @NonNull FeedstockBatchBlendProfileTargetSelection toTarget(
      @NonNull FeedstockBatchBlendProfileTargetRequest request
  ) {
    requireNonNull(request, "feedstock batch blend profile target request cannot be null");
    return switch (request) {
      case PredefinedZoneFeedstockBatchBlendProfileTargetRequest predefined ->
          new PredefinedFeedstockBatchBlendProfileTargetSelection(predefined.zone());
      case CustomRectangleFeedstockBatchBlendProfileTargetRequest custom ->
          new CustomRectangleFeedstockBatchBlendProfileTargetSelection(new RectangleSugarAcidityTargetGeometry(
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
      @Nullable TanninTargetRangeRequest request
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

  private static @NonNull ProfileAxisNormalizerCode normalizerFor(
      @NonNull ProfileTargetScoringPurpose purpose
  ) {
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    return switch (purpose) {
      case GRAPH_EXPLORATION -> ProfileAxisNormalizerCode.MIN_MAX;
      case BLENDING_PLANNING -> ProfileAxisNormalizerCode.EFFECTIVE_RANGE;
      case TARGET_ZONE_MATCHING -> ProfileAxisNormalizerCode.THRESHOLD_SEGMENT;
      case EXPERT_CALIBRATION -> ProfileAxisNormalizerCode.SENSITIVITY_UNIT;
    };
  }
}
