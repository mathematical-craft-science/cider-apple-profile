package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.ScoreFeedstockBatchProfileTargetUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchProfileTargetScoreBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.OptimizeFeedstockBatchCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.OptimizeFeedstockBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.ScoreFeedstockBatchProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.mapper.FeedstockBatchProfileTargetScoreRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class FeedstockBatchCompositionPlanningController implements FeedstockBatchCompositionPlanningApi {

  private final @NonNull ScoreFeedstockBatchProfileTargetUseCase scoreProfileTargetUseCase;
  private final @NonNull OptimizeFeedstockBatchCompositionUseCase optimizeFeedstockBatchCompositionUseCase;

  @Override
  @PostMapping(
      value = "/feedstock-batches/profile-target-scores",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull FeedstockBatchProfileTargetScoreBatchResponse> scoreProfileTarget(
      @RequestBody @NonNull ScoreFeedstockBatchProfileTargetRequest request
  ) {
    final var scores = scoreProfileTargetUseCase.score(
        FeedstockBatchProfileTargetScoreRestMapper.toCommand(request)
    );

    return ResponseEntity.ok(
        FeedstockBatchProfileTargetScoreRestMapper.toResponse(scores)
    );
  }

  @Override
  @PostMapping(
      value = "/feedstock-batches/composition-optimizations",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull OptimizeFeedstockBatchCompositionResponse> optimizeComposition(
      @RequestBody @NonNull OptimizeFeedstockBatchCompositionRequest request
  ) {
    final var result = optimizeFeedstockBatchCompositionUseCase.optimize(
        FeedstockBatchProfileTargetScoreRestMapper.toOptimizationCommand(request)
    );

    return ResponseEntity.ok(
        FeedstockBatchProfileTargetScoreRestMapper.toResponse(result, stepPercent(request))
    );
  }

  private static int stepPercent(@NonNull OptimizeFeedstockBatchCompositionRequest request) {
    return requireNonNullElse( // NOSONAR
        request.stepPercent(),
        DiscretizedCompositionOptimizationSettings.DEFAULT_STEP_PERCENT
    );
  }
}
