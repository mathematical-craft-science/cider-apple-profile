package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.AssessFeedstockBatchCurrentSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchGraphPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper.FeedstockBatchSugarAcidityRestMapper;
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
public final class FeedstockBatchSugarAcidityController implements FeedstockBatchSugarAcidityApi {

  private final @NonNull AssessFeedstockBatchCurrentSugarAcidityZoneUseCase assessSugarAcidityZoneUseCase;
  private final @NonNull QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase querySugarAcidityZonePointsUseCase;

  @Override
  @GetMapping(value = "/feedstock-batches/{batchId}/sugar-acidity-zone", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockBatchGraphPointResponse> assessSugarAcidityZone(
      @PathVariable @NonNull UUID batchId
  ) {
    final var assessment = assessSugarAcidityZoneUseCase.assess(
        FeedstockBatchId.of(batchId)
    );

    return ResponseEntity.ok(
        FeedstockBatchSugarAcidityRestMapper.toResponse(assessment)
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
        FeedstockBatchSugarAcidityRestMapper.toQuery(request)
    );

    return ResponseEntity.ok(
        FeedstockBatchSugarAcidityRestMapper.toResponse(results)
    );
  }
}
