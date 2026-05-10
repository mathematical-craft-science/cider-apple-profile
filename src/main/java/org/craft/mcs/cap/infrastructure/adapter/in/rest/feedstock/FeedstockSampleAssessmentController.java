package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.AssessFeedstockSampleSugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.ListFeedstockSampleSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredictFeedstockSamplePotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSamplePotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper.FeedstockSamplePotentialAbvRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper.FeedstockSampleSugarAcidityZoneRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class FeedstockSampleAssessmentController implements FeedstockSampleAssessmentApi {

  private final @NonNull AssessFeedstockSampleSugarAcidityZoneUseCase assessSugarAcidityZoneUseCase;
  private final @NonNull ListFeedstockSampleSugarAcidityZonePointsUseCase listSugarAcidityZonePointsUseCase;
  private final @NonNull PredictFeedstockSamplePotentialAbvUseCase predictPotentialAbvUseCase;

  @Override
  @GetMapping(value = "/feedstock-samples/{sampleId}/sugar-acidity-zone", produces = APPLICATION_JSON_VALUE)
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

  @Override
  @GetMapping(value = "/feedstock-samples/sugar-acidity-zone-points", produces = APPLICATION_JSON_VALUE)
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

  @Override
  @GetMapping(value = "/feedstock-samples/{sampleId}/abv-prediction", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockSamplePotentialAbvResponse> predictPotentialAbv(
      @PathVariable @NonNull UUID sampleId
  ) {
    final var prediction = predictPotentialAbvUseCase.predict(
        FeedstockSampleId.of(sampleId)
    );

    return ResponseEntity.ok(
        FeedstockSamplePotentialAbvRestMapper.toResponse(prediction)
    );
  }
}
