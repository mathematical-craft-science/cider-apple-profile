package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.AssessFeedstockMeasurementSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.QueryFeedstockMeasurementSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper.FeedstockMeasurementSugarAcidityZoneRestMapper;
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
public final class FeedstockMeasurementSugarAcidityController implements FeedstockMeasurementSugarAcidityApi {

  private final @NonNull AssessFeedstockMeasurementSugarAcidityZoneUseCase assessSugarAcidityZoneUseCase;
  private final @NonNull QueryFeedstockMeasurementSugarAcidityZonePointsUseCase querySugarAcidityZonePointsUseCase;

  @Override
  @GetMapping(value = "/feedstock-measurements/{measurementId}/sugar-acidity-zone", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockMeasurementSugarAcidityZoneResponse> assessSugarAcidityZone(
      @PathVariable @NonNull UUID measurementId
  ) {
    final var assessment = assessSugarAcidityZoneUseCase.assess(
        FeedstockMeasurementId.of(measurementId)
    );

    return ResponseEntity.ok(
        FeedstockMeasurementSugarAcidityZoneRestMapper.toResponse(assessment)
    );
  }

  @Override
  @PostMapping(
      value = "/feedstock-measurements/sugar-acidity-zone-points",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull FeedstockMeasurementSugarAcidityZonePointBatchResponse> listSugarAcidityZonePoints(
      @RequestBody @NonNull FeedstockMeasurementSugarAcidityZonePointQueryRequest request
  ) {
    final var results = querySugarAcidityZonePointsUseCase.query(
        FeedstockMeasurementSugarAcidityZoneRestMapper.toQuery(request)
    );

    return ResponseEntity.ok(
        FeedstockMeasurementSugarAcidityZoneRestMapper.toResponse(results)
    );
  }
}
