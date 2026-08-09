package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.model.CompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper.CompositionCommonResponseMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchCompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchCompositionOptimizationSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.OptimizeFeedstockBatchCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.OptimizeFeedstockBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionOptimizationAssessmentRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper
    .toBatchCriteria;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper
    .toSeason;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphCriteriaRestMapper
    .toZoneCriteria;


/** Maps the Feedstock composition-optimization transport contract. */
public final class FeedstockBatchCompositionOptimizationRestMapper {

  private FeedstockBatchCompositionOptimizationRestMapper() {
  }

  public static @NonNull OptimizeFeedstockBatchCompositionCommand toCommand(
      @NonNull OptimizeFeedstockBatchCompositionRequest request
  ) {
    requireNonNull(request, "optimize feedstock batch composition request cannot be null");
    final var target = requireNonNull(request.target(), "feedstock batch profile target cannot be null");
    final var settings = new DiscretizedCompositionOptimizationSettings(
        requireNonNullElse(
            request.stepPercent(),
            DiscretizedCompositionOptimizationSettings.DEFAULT_STEP_PERCENT
        ),
        requireNonNullElse(request.limit(), DiscretizedCompositionOptimizationSettings.DEFAULT_LIMIT)
    );
    final var mode = requireNonNull(request.mode(), "composition optimization mode cannot be null");
    final var algorithm = requireNonNullElse(
        request.algorithm(),
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH
    );
    final var calculator = requireNonNullElse(
        request.calculator(),
        CompositionCalculatorCode.MASS_BALANCE
    );
    final var normalizer = requireNonNullElse(
        request.normalizer(),
        CompositionOptimizationAssessmentRestMapper.defaultOptimizationNormalizer()
    );
    final var objectiveFunction = requireNonNullElse(
        request.objectiveFunction(),
        CompositionOptimizationAssessmentRestMapper.defaultOptimizationObjectiveFunction()
    );

    return new OptimizeFeedstockBatchCompositionCommand( // NOSONAR
        WorkspaceId.of(request.workspaceId()),
        new FeedstockAnalysisCandidateFilters(
            toBatchCriteria(request.batchIds()),
            toSeason(request.season())
        ),
        toZoneCriteria(request.zone()),
        FeedstockProfileTargetRestMapper.toSelection(target),
        FeedstockProfileTargetRestMapper.toTanninTarget(request.tanninTarget()),
        new ProfileTargetScoringConfiguration(
            requireNonNull(normalizer, "profile target normalizer cannot be null"),
            requireNonNull(objectiveFunction, "profile target objective function cannot be null"),
            ProfileTargetRestMapper.toWeights(request.weights())
        ),
        CompositionOptimizationAssessmentRestMapper.toCriteria(request.yanReadiness()),
        settings,
        mode,
        algorithm,
        calculator,
        toVolume(request.targetTotalVolumeLiters()),
        requireNonNullElse(request.sourceLimit(), QueryLimits.MAXIMUM)
    );
  }

  public static @NonNull OptimizeFeedstockBatchCompositionResponse toResponse(
      @NonNull OptimizeFeedstockBatchCompositionResult result
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
        optimization.settings().stepPercent(),
        optimization.evaluatedCandidates(),
        optimization.candidates().stream()
            .map(FeedstockBatchCompositionOptimizationRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockBatchCompositionOptimizationCandidateResponse toResponse(
      @NonNull CompositionOptimizationCandidateView view
  ) {
    requireNonNull(view, "composition optimization candidate view cannot be null");
    final var candidate = view.candidate();
    final var composition = candidate.composition();

    return new FeedstockBatchCompositionOptimizationCandidateResponse(
        composition.sources().stream()
            .map(FeedstockBatchCompositionOptimizationRestMapper::toSourceResponse)
            .toList(),
        CompositionCommonResponseMapper.toCharacteristicsResponse(
            composition.characteristics()
        ),
        toVolumeResponse(composition.totalVolume()),
        List.copyOf(composition.warnings()),
        ProfileTargetRestMapper.toResponse(candidate.distance()),
        CompositionOptimizationAssessmentRestMapper.toResponse(candidate.objectiveAssessment())
    );
  }

  private static @NonNull FeedstockBatchCompositionOptimizationSourceResponse toSourceResponse(
      @NonNull CompositionSource source
  ) {
    requireNonNull(source, "composition source cannot be null");
    final var requestedVolume = requestedVolume(source);
    return new FeedstockBatchCompositionOptimizationSourceResponse(
        UUID.fromString(source.reference().id()),
        toRequestedVolumeResponse(requestedVolume),
        proportion(source)
    );
  }

  private static @Nullable BigDecimal requestedVolume(@NonNull CompositionSource source) {
    return source.weight() instanceof AbsoluteCompositionVolume(VolumeValue volume) ? volume.liters() : null;
  }

  private static @Nullable BigDecimal proportion(@NonNull CompositionSource source) {
    return source.weight() instanceof CompositionProportion(BigDecimal value) ? value : null;
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
}
