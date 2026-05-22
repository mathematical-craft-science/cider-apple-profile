package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.feedstock.application.port.in.AssessFeedstockBatchCurrentFermentationStartReadinessUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.AssessFeedstockBatchCurrentSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.OptimizeFeedstockBatchCompositionUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredictFeedstockBatchCurrentPotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchProfileTargetUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentFermentationStartReadinessResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchProfileTargetScoreBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.OptimizeFeedstockBatchCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.OptimizeFeedstockBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.ScoreFeedstockBatchProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper.FeedstockBatchProfileTargetScoreRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper.FeedstockBatchCurrentAssessmentRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class FeedstockBatchCurrentAssessmentController implements FeedstockBatchCurrentAssessmentApi {

  private final @NonNull AssessFeedstockBatchCurrentSugarAcidityZoneUseCase assessSugarAcidityZoneUseCase;
  private final @NonNull QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase querySugarAcidityZonePointsUseCase;
  private final @NonNull PredictFeedstockBatchCurrentPotentialAbvUseCase predictPotentialAbvUseCase;
  private final @NonNull AssessFeedstockBatchCurrentFermentationStartReadinessUseCase
      assessFermentationStartReadinessUseCase;
  private final @NonNull ScoreFeedstockBatchProfileTargetUseCase scoreProfileTargetUseCase;
  private final @NonNull OptimizeFeedstockBatchCompositionUseCase optimizeFeedstockBatchCompositionUseCase;

  @Override
  @GetMapping(value = "/feedstock-batches/{batchId}/sugar-acidity-zone", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentSugarAcidityZoneResponse> assessSugarAcidityZone(
      @PathVariable @NonNull UUID batchId
  ) {
    final var assessment = assessSugarAcidityZoneUseCase.assess(
        FeedstockBatchId.of(batchId)
    );

    return ResponseEntity.ok(
        FeedstockBatchCurrentAssessmentRestMapper.toResponse(assessment)
    );
  }

  @Override
  @PostMapping(
      value = "/feedstock-batches/sugar-acidity-zone-points",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentSugarAcidityZonePointBatchResponse> listSugarAcidityZonePoints(
      @RequestBody @NonNull FeedstockBatchCurrentSugarAcidityZonePointQueryRequest request
  ) {
    final var results = querySugarAcidityZonePointsUseCase.query(
        FeedstockBatchCurrentAssessmentRestMapper.toQuery(request)
    );

    return ResponseEntity.ok(
        FeedstockBatchCurrentAssessmentRestMapper.toResponse(results)
    );
  }

  @Override
  @GetMapping(value = "/feedstock-batches/{batchId}/abv-prediction", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentPotentialAbvResponse> predictPotentialAbv(
      @PathVariable @NonNull UUID batchId
  ) {
    final var prediction = predictPotentialAbvUseCase.predict(
        FeedstockBatchId.of(batchId)
    );

    return ResponseEntity.ok(
        FeedstockBatchCurrentAssessmentRestMapper.toResponse(prediction)
    );
  }

  @Override
  @GetMapping(value = "/feedstock-batches/{batchId}/fermentation-start-readiness", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentFermentationStartReadinessResponse>
      assessFermentationStartReadiness(@PathVariable @NonNull UUID batchId) {
    final var assessment = assessFermentationStartReadinessUseCase.assess(
        FeedstockBatchId.of(batchId)
    );

    return ResponseEntity.ok(
        FeedstockBatchCurrentAssessmentRestMapper.toResponse(assessment)
    );
  }

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
    return request.stepPercent() == null
        ? DiscretizedCompositionOptimizationSettings.DEFAULT_STEP_PERCENT
        : request.stepPercent();
  }
}
