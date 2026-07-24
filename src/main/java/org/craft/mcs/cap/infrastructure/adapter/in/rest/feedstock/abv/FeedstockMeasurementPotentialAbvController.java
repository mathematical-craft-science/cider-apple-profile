package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.PredictFeedstockMeasurementPotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.dto.FeedstockMeasurementPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.mapper.FeedstockMeasurementPotentialAbvRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class FeedstockMeasurementPotentialAbvController implements FeedstockMeasurementPotentialAbvApi {

  private final @NonNull PredictFeedstockMeasurementPotentialAbvUseCase predictPotentialAbvUseCase;

  @Override
  @GetMapping(value = "/feedstock-measurements/{measurementId}/abv-prediction", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockMeasurementPotentialAbvResponse> predictPotentialAbv(
      @PathVariable @NonNull UUID measurementId
  ) {
    final var prediction = predictPotentialAbvUseCase.predict(
        FeedstockMeasurementId.of(measurementId)
    );

    return ResponseEntity.ok(
        FeedstockMeasurementPotentialAbvRestMapper.toResponse(prediction)
    );
  }
}
