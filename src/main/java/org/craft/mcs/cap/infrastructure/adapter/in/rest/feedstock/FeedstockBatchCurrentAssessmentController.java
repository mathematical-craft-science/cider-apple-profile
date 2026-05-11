package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.AssessFeedstockBatchCurrentSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredictFeedstockBatchCurrentPotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchBlendProfileTargetUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchBlendProfileTargetScoreBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.ScoreFeedstockBatchBlendProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper.FeedstockBatchBlendProfileTargetScoreRestMapper;
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
public final class FeedstockBatchCurrentAssessmentController {

  private final @NonNull AssessFeedstockBatchCurrentSugarAcidityZoneUseCase assessSugarAcidityZoneUseCase;
  private final @NonNull QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase querySugarAcidityZonePointsUseCase;
  private final @NonNull PredictFeedstockBatchCurrentPotentialAbvUseCase predictPotentialAbvUseCase;
  private final @NonNull ScoreFeedstockBatchBlendProfileTargetUseCase scoreBlendProfileTargetUseCase;

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

  @PostMapping(
      value = "/feedstock-batches/blend-profile-target-scores",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull FeedstockBatchBlendProfileTargetScoreBatchResponse> scoreBlendProfileTarget(
      @RequestBody @NonNull ScoreFeedstockBatchBlendProfileTargetRequest request
  ) {
    final var scores = scoreBlendProfileTargetUseCase.score(
        FeedstockBatchBlendProfileTargetScoreRestMapper.toCommand(request)
    );

    return ResponseEntity.ok(
        FeedstockBatchBlendProfileTargetScoreRestMapper.toResponse(scores)
    );
  }
}
