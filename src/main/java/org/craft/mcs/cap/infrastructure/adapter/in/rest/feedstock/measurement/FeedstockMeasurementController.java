package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement;

import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.PatchFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.UpdateFeedstockMeasurementRequest;
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

  private final @NonNull FeedstockMeasurementCompatibilityFacade compatibilityFacade;

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
    final var created = compatibilityFacade.create(batchId, request);

    return ResponseEntity
        .created(URI.create("/feedstock-batches/" + batchId + "/measurements/" + created.measurementId()))
        .body(created);
  }

  @Override
  @GetMapping(value = "/feedstock-batches/{batchId}/measurements", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockMeasurementListResponse> listByBatchId(
      @PathVariable @NonNull UUID batchId
  ) {
    return ResponseEntity.ok(new FeedstockMeasurementListResponse(compatibilityFacade.list(batchId)));
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
    return ResponseEntity.ok(compatibilityFacade.replace(batchId, measurementId, request));
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
    return ResponseEntity.ok(compatibilityFacade.patch(batchId, measurementId, request));
  }

  @DeleteMapping(value = "/feedstock-batches/{batchId}/measurements/{measurementId}")
  @Override
  public @NonNull ResponseEntity<Void> delete(
      @PathVariable @NonNull UUID batchId,
      @PathVariable @NonNull UUID measurementId
  ) {
    compatibilityFacade.retract(batchId, measurementId);

    return ResponseEntity.noContent().build();
  }
}
