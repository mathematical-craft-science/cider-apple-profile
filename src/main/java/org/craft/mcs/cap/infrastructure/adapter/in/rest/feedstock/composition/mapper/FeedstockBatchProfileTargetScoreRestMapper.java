package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.ScoreFeedstockBatchProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionResult;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.model.ChemicalProfileCompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchCompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchCompositionOptimizationSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchProfileTargetScoreBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchProfileTargetScoreResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.OptimizeFeedstockBatchCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.OptimizeFeedstockBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.ScoreFeedstockBatchProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionOptimizationRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


public final class FeedstockBatchProfileTargetScoreRestMapper {

  private FeedstockBatchProfileTargetScoreRestMapper() {
  }

  public static @NonNull ScoreFeedstockBatchProfileTargetCommand toCommand(
      @NonNull ScoreFeedstockBatchProfileTargetRequest request
  ) {
    requireNonNull(request, "score feedstock batch profile target request cannot be null");
    final var target = requireNonNull(request.target(), "feedstock batch profile target cannot be null");
    final var purpose = requireNonNull(request.purpose(), "profile target scoring purpose cannot be null");

    return new ScoreFeedstockBatchProfileTargetCommand(
        WorkspaceId.of(request.workspaceId()),
        new FeedstockMeasurementPointFilters(
            toBatchCriteria(request.batchId()),
            toSeason(request.season()),
            toZoneCriteria(request.zone()),
            toCapabilityCriteria(request.capability(), request.capable())
        ),
        purpose,
        toTarget(target),
        toTanninTarget(request.tanninTarget()),
        toWeights(request.weights()),
        requireNonNullElse(request.limit(), QueryLimits.DEFAULT)
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
    final var purpose = purpose(request);
    final var target = requireNonNull(request.target(), "feedstock batch profile target cannot be null");
    final var settings = new DiscretizedCompositionOptimizationSettings(
        requireNonNullElse(request.stepPercent(), DiscretizedCompositionOptimizationSettings.DEFAULT_STEP_PERCENT),
        requireNonNullElse(request.limit(), DiscretizedCompositionOptimizationSettings.DEFAULT_LIMIT)
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

    return new OptimizeFeedstockBatchCompositionCommand( // NOSONAR
        WorkspaceId.of(request.workspaceId()),
        new FeedstockMeasurementPointFilters(
            toBatchCriteria(request.batchIds()),
            toSeason(request.season()),
            toZoneCriteria(request.zone()),
            toCapabilityCriteria(request.capability(), request.capable())
        ),
        purpose,
        toTarget(target),
        toTanninTarget(request.tanninTarget()),
        toWeights(request.weights()),
        CompositionOptimizationRestMapper.toCriteria(request.yanReadiness()),
        settings,
        mode,
        algorithm,
        calculator,
        normalizer,
        objectiveFunction,
        toVolume(request.targetTotalVolumeLiters()),
        requireNonNullElse(request.sourceLimit(), QueryLimits.MAXIMUM)
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
        optimization.evaluatedCandidates(),
        optimization.candidates().stream()
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
        score.workspaceId().value(),
        score.currentMeasurementId().value(),
        score.currentMeasurementName().value(),
        SugarAcidityZoneRestMapper.toResponse(score.point()),
        ProfileTargetRestMapper.toResponse(score.distance())
    );
  }

  private static @NonNull FeedstockBatchCompositionOptimizationCandidateResponse toResponse(
      @NonNull ChemicalProfileCompositionOptimizationCandidateView view
  ) {
    requireNonNull(view, "chemical profile composition optimization candidate view cannot be null");
    final var candidate = view.candidate();
    final var composition = candidate.composition();

    return new FeedstockBatchCompositionOptimizationCandidateResponse(
        composition.sources().stream()
            .map(FeedstockBatchProfileTargetScoreRestMapper::toSourceResponse)
            .toList(),
        SugarAcidityZoneRestMapper.toResponse(composition.profile()),
        toYeastAssimilableNitrogenResponse(composition.yeastAssimilableNitrogen()),
        toVolumeResponse(composition.totalVolume()),
        List.copyOf(composition.warnings()),
        ProfileTargetRestMapper.toResponse(candidate.distance()),
        CompositionOptimizationRestMapper.toResponse(candidate.objectiveAssessment())
    );
  }

  private static @NonNull FeedstockBatchCompositionOptimizationSourceResponse toSourceResponse(
      @NonNull ChemicalProfileCompositionSource source
  ) {
    requireNonNull(source, "chemical profile composition source cannot be null");
    final var requestedVolume = requestedVolume(source);
    return new FeedstockBatchCompositionOptimizationSourceResponse(
        UUID.fromString(source.reference().id()),
        toRequestedVolumeResponse(requestedVolume),
        proportion(source)
    );
  }

  private static @Nullable FeedstockBatchCriteria toBatchCriteria(@Nullable UUID batchId) {
    return batchId == null ? null : FeedstockBatchCriteria.of(FeedstockBatchId.of(batchId));
  }

  private static @Nullable BigDecimal requestedVolume(@NonNull ChemicalProfileCompositionSource source) {
    return source.weight() instanceof AbsoluteCompositionVolume(VolumeValue volume) ? volume.liters() : null;
  }

  private static @Nullable BigDecimal proportion(@NonNull ChemicalProfileCompositionSource source) {
    return source.weight() instanceof CompositionProportion(BigDecimal value) ? value : null;
  }

  private static @Nullable SugarAcidityAxisPointResponse toYeastAssimilableNitrogenResponse(
      @Nullable AxisValue point
  ) {
    return point == null
        ? null
        : SugarAcidityZoneRestMapper.toResponse(
            point,
            CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
        );
  }

  private static @Nullable DecimalQuantityResponse toVolumeResponse(@Nullable VolumeValue volume) {
    return volume == null ? null : new DecimalQuantityResponse(volume.liters(), CanonicalUnitLabels.VOLUME);
  }

  private static @Nullable DecimalQuantityResponse toRequestedVolumeResponse(@Nullable BigDecimal volume) {
    return volume == null ? null : new DecimalQuantityResponse(volume, CanonicalUnitLabels.VOLUME);
  }

  private static @Nullable VolumeValue toVolume(@Nullable BigDecimal liters) {
    return liters == null ? null : VolumeValue.ofLiters(liters);
  }

  private static @NonNull ProfileTargetScoringPurpose purpose(
      @NonNull OptimizeFeedstockBatchCompositionRequest request
  ) {
    final var purpose = request.purpose();
    return requireNonNullElse(purpose, ProfileTargetScoringPurpose.BLENDING_PLANNING); // NOSONAR
  }

  private static @Nullable SugarAcidityZoneCriteria toZoneCriteria(@Nullable SugarAcidityZone zone) {
    return zone == null ? null : SugarAcidityZoneCriteria.of(zone);
  }

  private static @Nullable ChemicalProfileCapabilityCriteria toCapabilityCriteria(
      @Nullable ChemicalProfileCapability capability,
      @Nullable Boolean capable
  ) {
    return capability == null ? null : new ChemicalProfileCapabilityCriteria(capability, capable);
  }

  private static @NonNull FeedstockProfileTargetSelection toTarget(
      @NonNull ProfileTargetRequest request
  ) {
    requireNonNull(request, "feedstock batch profile target request cannot be null");
    return switch (request) {
      case PredefinedZoneProfileTargetRequest(_, var zone) ->
          new PredefinedFeedstockProfileTargetSelection(zone);
      case CustomRectangleProfileTargetRequest(_, var sugarMin, var sugarMax, var acidityMin, var acidityMax) ->
          new CustomRectangleFeedstockProfileTargetSelection(new RectangleSugarAcidityTargetGeometry(
              sugarMin,
              sugarMax,
              acidityMin,
              acidityMax
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

    return new ProfileTargetObjectiveWeights(
        weightOrDefault(request.sugar()),
        weightOrDefault(request.acidity()),
        request.tannin()
    );
  }

  private static @Nullable FeedstockSeason toSeason(@Nullable FeedstockSeasonRequest request) {
    return request == null ? null : FeedstockSeason.of(request.startsOn(), request.endsOn());
  }

  private static @NonNull BigDecimal weightOrDefault(@Nullable BigDecimal value) {
    return requireNonNullElse(value, BigDecimal.ONE); // NOSONAR
  }

}
