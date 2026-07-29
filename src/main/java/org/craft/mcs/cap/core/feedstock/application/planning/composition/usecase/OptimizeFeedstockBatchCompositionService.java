package org.craft.mcs.cap.core.feedstock.application.planning.composition.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.service.FeedstockGraphCandidateMatcher;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionUseCase;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceCharacteristics;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationSource;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.InsufficientFeedstockCompositionSourcesException;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockCurrentProfileAnalysisCandidateRepository;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionResult;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Builds a composition optimization problem from current feedstock batch measurements.
 *
 * <p>The service projects the feedstock bounded context into the generic profile-analysis optimization
 * model: current batch profiles become optimization sources, batch available volume becomes the
 * source bound, and the selected target is passed to the shared composition optimizer.
 */
@Component
@RequiredArgsConstructor
public final class OptimizeFeedstockBatchCompositionService implements OptimizeFeedstockBatchCompositionUseCase {

  private static final int MINIMUM_SOURCE_COUNT = 2;
  private static final @NonNull String SOURCE_TYPE = "FEEDSTOCK_BATCH";

  private final @NonNull FeedstockCurrentProfileAnalysisCandidateRepository candidateRepository;
  private final @NonNull FeedstockBatchSummaryQueryRepository feedstockBatchQueryRepository;
  private final @NonNull EvaluateJolicoeurSugarAcidityGraphUseCase graphEvaluator;
  private final @NonNull FeedstockGraphCandidateMatcher graphCandidateMatcher;
  private final @NonNull SugarAcidityTargetResolver targetResolver;
  private final @NonNull OptimizeCompositionUseCase optimizeCompositionUseCase;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;

  @Override
  public @NonNull OptimizeFeedstockBatchCompositionResult optimize(
      @NonNull OptimizeFeedstockBatchCompositionCommand command
  ) {
    requireNonNull(command, "optimize feedstock batch composition command cannot be null");
    feedstockAccessControl.requireReadAccess(command.workspaceId());

    final var batchesById = feedstockBatchQueryRepository.findAllByWorkspaceId(command.workspaceId())
        .stream()
        .collect(Collectors.toMap(FeedstockBatchSummary::batchId, Function.identity()));
    final var sources = candidateRepository.findCandidates(
            new CurrentFeedstockBatchAnalysisCandidateQuery(
                command.workspaceId(),
                command.filters().batch(),
                command.filters().season(),
                null,
                command.sourceLimit()
            )
        )
        .stream()
        .map(measurement -> toOptimizationSourceIfUsable(measurement, command.filters(), batchesById))
        .flatMap(Optional::stream)
        .toList();
    if (sources.size() < MINIMUM_SOURCE_COUNT) {
      throw new InsufficientFeedstockCompositionSourcesException(sources.size(), MINIMUM_SOURCE_COUNT);
    }

    final var optimization = optimizeCompositionUseCase.optimize(
        new OptimizeCompositionCommand(
            sources,
            command.tanninTarget() == null
                ? CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
                : CompositionTanninPolicy.REQUIRE_TANNIN,
            objective(command),
            command.scoringConfiguration(),
            command.criteria(),
            command.settings(),
            command.mode(),
            command.algorithm(),
            command.calculatorCode(),
            command.targetTotalVolume()
        )
    );

    return new OptimizeFeedstockBatchCompositionResult(sources.size(), optimization);
  }

  private @NonNull Optional<@NonNull CompositionOptimizationSource> toOptimizationSourceIfUsable(
      @NonNull FeedstockCurrentProfileView currentProfile,
      @NonNull FeedstockAnalysisCandidateFilters filters,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockBatchSummary> batchesById
  ) {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    final var graphEvaluation = graphEvaluator.evaluate(
        currentProfile.specificGravity(),
        currentProfile.titratableAcidity()
    );
    if (!graphEvaluation.plottable()) {
      return Optional.empty();
    }
    if (!graphCandidateMatcher.matches(currentProfile, graphEvaluation, filters)) {
      return Optional.empty();
    }
    final var specificGravity = currentProfile.specificGravity();
    final var titratableAcidity = currentProfile.titratableAcidity();
    if (specificGravity == null || titratableAcidity == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(batchesById.get(currentProfile.batchId()))
        .map(batch -> new CompositionOptimizationSource(
                new CompositionSourceReference(
                    SOURCE_TYPE,
                    currentProfile.batchId().value().toString()
                ),
                new CompositionSourceCharacteristics(
                    specificGravity,
                    titratableAcidity,
                    currentProfile.tannin(),
                    currentProfile.yeastAssimilableNitrogen()
                ),
                batch.availableVolume()
            ));
  }

  private @NonNull ProfileTargetObjective objective(
      @NonNull OptimizeFeedstockBatchCompositionCommand command
  ) {
    final SugarAcidityTarget target = target(command);
    final var tanninTarget = command.tanninTarget();
    if (tanninTarget == null) {
      return ProfileTargetObjective.of(target);
    }

    return ProfileTargetObjective.withTanninTarget(target, tanninTarget);
  }

  private @NonNull SugarAcidityTarget target(
      @NonNull OptimizeFeedstockBatchCompositionCommand command
  ) {
    return switch (command.target()) {
      case PredefinedFeedstockProfileTargetSelection(JolicoeurSugarAcidityZone zone) ->
          targetResolver.resolvePredefinedZone(zone);
      case CustomRectangleFeedstockProfileTargetSelection(
          RectangleSugarAcidityTargetGeometry geometry
      ) ->
          new CustomRectangleSugarAcidityTarget(geometry);
    };
  }
}
