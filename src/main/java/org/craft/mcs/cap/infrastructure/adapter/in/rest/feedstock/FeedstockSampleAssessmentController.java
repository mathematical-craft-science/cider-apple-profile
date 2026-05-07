package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.AssessFeedstockSampleSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.ListFeedstockSampleSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper.FeedstockSampleSugarAcidityZoneRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public final class FeedstockSampleAssessmentController {

  private final @NonNull AssessFeedstockSampleSugarAcidityZoneUseCase assessSugarAcidityZoneUseCase;
  private final @NonNull ListFeedstockSampleSugarAcidityZonePointsUseCase listSugarAcidityZonePointsUseCase;

  @GetMapping("/feedstock-samples/{sampleId}/sugar-acidity-zone")
  public @NonNull ResponseEntity<@NonNull FeedstockSampleSugarAcidityZoneResponse> assessSugarAcidityZone(
      @PathVariable @NonNull UUID sampleId
  ) {
    final var assessment = assessSugarAcidityZoneUseCase.assess(
        FeedstockSampleId.of(sampleId)
    );

    return ResponseEntity.ok(
        FeedstockSampleSugarAcidityZoneRestMapper.toResponse(assessment)
    );
  }

  @GetMapping("/feedstock-samples/sugar-acidity-zone-points")
  public @NonNull ResponseEntity<@NonNull FeedstockSampleSugarAcidityZonePointBatchResponse> listSugarAcidityZonePoints(
      @RequestParam @NonNull UUID ciderMakerId
  ) {
    final var results = listSugarAcidityZonePointsUseCase.listByCiderMakerId(
            CiderMakerId.of(ciderMakerId)
        );

    return ResponseEntity.ok(
        FeedstockSampleSugarAcidityZoneRestMapper.toResponse(results)
    );
  }
}
