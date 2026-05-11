package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockMeasurementUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.ListFeedstockMeasurementsUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper.FeedstockMeasurementRestMapper;
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
public final class FeedstockMeasurementController implements FeedstockMeasurementApi {

  private final @NonNull CreateFeedstockMeasurementUseCase createFeedstockMeasurementUseCase;
  private final @NonNull ListFeedstockMeasurementsUseCase listFeedstockMeasurementsUseCase;

  @Override
  @PostMapping(
      value = "/feedstock-batches/{batchId}/measurements",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull FeedstockMeasurementResponse> create(
      @PathVariable @NonNull UUID batchId,
      @RequestBody @NonNull CreateFeedstockMeasurementRequest request
  ) {
    final var created = createFeedstockMeasurementUseCase.create(
        FeedstockMeasurementRestMapper.toCommand(FeedstockBatchId.of(batchId), request)
    );

    return ResponseEntity
        .created(URI.create("/feedstock-batches/" + batchId + "/measurements/" + created.measurementId().value()))
        .body(FeedstockMeasurementRestMapper.toResponse(created));
  }

  @Override
  @GetMapping(value = "/feedstock-batches/{batchId}/measurements", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockMeasurementListResponse> listByBatchId(
      @PathVariable @NonNull UUID batchId
  ) {
    final var measurements = listFeedstockMeasurementsUseCase.listByBatchId(FeedstockBatchId.of(batchId));

    return ResponseEntity.ok(new FeedstockMeasurementListResponse(
        measurements.stream()
            .map(FeedstockMeasurementRestMapper::toResponse)
            .toList()
    ));
  }
}
