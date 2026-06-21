package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement;

import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.CreateFeedstockMeasurementUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.DeleteFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.DeleteFeedstockMeasurementUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.ListFeedstockMeasurementsUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.UpdateFeedstockMeasurementUseCase;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.PatchFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.UpdateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.mapper.FeedstockMeasurementRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class FeedstockMeasurementController implements FeedstockMeasurementApi {

  private final @NonNull CreateFeedstockMeasurementUseCase createFeedstockMeasurementUseCase;
  private final @NonNull ListFeedstockMeasurementsUseCase listFeedstockMeasurementsUseCase;
  private final @NonNull UpdateFeedstockMeasurementUseCase updateFeedstockMeasurementUseCase;
  private final @NonNull DeleteFeedstockMeasurementUseCase deleteFeedstockMeasurementUseCase;

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

  @PutMapping(
      value = "/feedstock-batches/{batchId}/measurements/{measurementId}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  @Override
  public @NonNull ResponseEntity<@NonNull FeedstockMeasurementResponse> update(
      @PathVariable @NonNull UUID batchId,
      @PathVariable @NonNull UUID measurementId,
      @RequestBody @NonNull UpdateFeedstockMeasurementRequest request
  ) {
    final var updated = updateFeedstockMeasurementUseCase.update(
        FeedstockMeasurementRestMapper.toCommand(
            FeedstockBatchId.of(batchId),
            FeedstockMeasurementId.of(measurementId),
            request
        )
    );

    return ResponseEntity.ok(FeedstockMeasurementRestMapper.toResponse(updated));
  }

  @PatchMapping(
      value = "/feedstock-batches/{batchId}/measurements/{measurementId}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  @Override
  public @NonNull ResponseEntity<@NonNull FeedstockMeasurementResponse> patch(
      @PathVariable @NonNull UUID batchId,
      @PathVariable @NonNull UUID measurementId,
      @RequestBody @NonNull PatchFeedstockMeasurementRequest request
  ) {
    final var updated = updateFeedstockMeasurementUseCase.update(
        FeedstockMeasurementRestMapper.toCommand(
            FeedstockBatchId.of(batchId),
            FeedstockMeasurementId.of(measurementId),
            request
        )
    );

    return ResponseEntity.ok(FeedstockMeasurementRestMapper.toResponse(updated));
  }

  @DeleteMapping(value = "/feedstock-batches/{batchId}/measurements/{measurementId}")
  @Override
  public @NonNull ResponseEntity<Void> delete(
      @PathVariable @NonNull UUID batchId,
      @PathVariable @NonNull UUID measurementId
  ) {
    deleteFeedstockMeasurementUseCase.delete(new DeleteFeedstockMeasurementCommand(
        FeedstockBatchId.of(batchId),
        FeedstockMeasurementId.of(measurementId)
    ));

    return ResponseEntity.noContent().build();
  }
}
