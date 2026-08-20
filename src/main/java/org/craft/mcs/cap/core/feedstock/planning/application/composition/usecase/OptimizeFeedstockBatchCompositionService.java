package org.craft.mcs.cap.core.feedstock.planning.application.composition.usecase;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.planning.application.authorization.port.out.FeedstockPlanningAccessControl;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningCandidateQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningCandidateSnapshot;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.port.out.FeedstockPlanningCandidateProvider;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphCriteria;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.port.out.FeedstockPlanningGraphAnalyzer;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Source;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.port.out.FeedstockCompositionOptimizer;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.InsufficientFeedstockCompositionSourcesException;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.port.in.OptimizeFeedstockBatchCompositionResult;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.port.in.OptimizeFeedstockBatchCompositionUseCase;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Builds a composition optimization problem from current feedstock batch measurements.
 *
   * <p>The service projects current Feedstock candidates into a consumer-owned optimization problem:
   * current batch profiles become optimization sources, batch available volume becomes the
   * source bound, and the selected target is passed through the outbound Formulation boundary.
 */
@Component
@RequiredArgsConstructor
public final class OptimizeFeedstockBatchCompositionService implements OptimizeFeedstockBatchCompositionUseCase {

  private static final int MINIMUM_SOURCE_COUNT = 2;
  private final @NonNull FeedstockPlanningCandidateProvider candidateProvider;
  private final @NonNull FeedstockPlanningGraphAnalyzer graphAnalyzer;
  private final @NonNull FeedstockCompositionOptimizer optimizer;
  private final @NonNull FeedstockPlanningAccessControl accessControl;

  @Override
  public @NonNull OptimizeFeedstockBatchCompositionResult optimize(
      @NonNull OptimizeFeedstockBatchCompositionCommand command
  ) {
    requireNonNull(command, "optimize feedstock batch composition command cannot be null");
    accessControl.requireReadAccess(command.workspaceId());
    final var sources = candidateProvider.findCandidates(
            new FeedstockPlanningCandidateQuery(
                command.workspaceId(),
                command.filters().batch(),
                command.filters().season(),
                null,
                command.sourceLimit()
            )
        )
        .stream()
        .map(candidate -> toOptimizationSourceIfUsable(candidate, command.sourceZoneCriteria()))
        .flatMap(Optional::stream)
        .toList();
    if (sources.size() < MINIMUM_SOURCE_COUNT) {
      throw new InsufficientFeedstockCompositionSourcesException(sources.size(), MINIMUM_SOURCE_COUNT);
    }

    final var optimization = optimizer.optimize(
        new FeedstockCompositionOptimizationRequest(
            sources,
            command.tanninTarget() == null
                ? FeedstockCompositionOptimizationRequest.TanninPolicy.CALCULATE_WHEN_COMPLETE
                : FeedstockCompositionOptimizationRequest.TanninPolicy.REQUIRE_TANNIN,
            command.target(),
            command.tanninTarget(),
            command.scoring(),
            command.criteria(),
            command.settings(),
            command.mode(),
            command.algorithm(),
            command.calculator(),
            command.targetTotalVolume()
        )
    );

    return new OptimizeFeedstockBatchCompositionResult(sources.size(), optimization);
  }

  private @NonNull Optional<@NonNull Source> toOptimizationSourceIfUsable(
      @NonNull FeedstockPlanningCandidateSnapshot candidate,
      @Nullable FeedstockPlanningGraphCriteria sourceZoneCriteria
  ) {
    final FeedstockCurrentProfileView currentProfile = candidate.currentProfile();
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    final var graphEvaluation = graphAnalyzer.evaluate(
        currentProfile.specificGravity(),
        currentProfile.titratableAcidity()
    );
    if (!graphEvaluation.plottable()) {
      return Optional.empty();
    }
    if (sourceZoneCriteria != null
        && !sourceZoneCriteria.matches(graphEvaluation)) {
      return Optional.empty();
    }
    final var specificGravity = currentProfile.specificGravity();
    final var titratableAcidity = currentProfile.titratableAcidity();
    if (specificGravity == null || titratableAcidity == null) {
      return Optional.empty();
    }

    return Optional.of(new FeedstockCompositionOptimizationRequest.Source(
        currentProfile.batchId(),
        new FeedstockCompositionOptimizationRequest.Characteristics(
            specificGravity,
            titratableAcidity,
            currentProfile.tannin(),
            currentProfile.yeastAssimilableNitrogen()
        ),
        candidate.availableVolume()
    ));
  }
}
