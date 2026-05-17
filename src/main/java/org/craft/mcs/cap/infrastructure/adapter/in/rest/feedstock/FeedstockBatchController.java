package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.DeleteFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.DeleteFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.ListFeedstockBatchesQuery;
import org.craft.mcs.cap.core.feedstock.application.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.UpdateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.PatchFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.UpdateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper.FeedstockBatchRestMapper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class FeedstockBatchController implements FeedstockBatchApi {

  private final @NonNull CreateFeedstockBatchUseCase createFeedstockBatchUseCase;
  private final @NonNull ListFeedstockBatchesUseCase listFeedstockBatchesUseCase;
  private final @NonNull UpdateFeedstockBatchUseCase updateFeedstockBatchUseCase;
  private final @NonNull DeleteFeedstockBatchUseCase deleteFeedstockBatchUseCase;

  @Override
  @PostMapping(value = "/feedstock-batches", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockBatchResponse> create(
      @RequestBody @NonNull CreateFeedstockBatchRequest request
  ) {
    final var created = createFeedstockBatchUseCase.create(
        FeedstockBatchRestMapper.toCommand(request)
    );

    return ResponseEntity
        .created(URI.create("/feedstock-batches/" + created.batchId().value()))
        .body(FeedstockBatchRestMapper.toResponse(created));
  }

  @Override
  @GetMapping(value = "/feedstock-batches", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockBatchListResponse> list(
      @RequestParam @NonNull UUID ciderMakerId,
      @RequestParam(required = false) @Nullable LocalDate seasonStartsOn,
      @RequestParam(required = false) @Nullable LocalDate seasonEndsOn
  ) {
    final var batches = listFeedstockBatchesUseCase.list(new ListFeedstockBatchesQuery(
        CiderMakerId.of(ciderMakerId),
        seasonOf(seasonStartsOn, seasonEndsOn)
    ));

    return ResponseEntity.ok(new FeedstockBatchListResponse(
        batches.stream()
            .map(FeedstockBatchRestMapper::toResponse)
            .toList()
    ));
  }

  @PutMapping(
      value = "/feedstock-batches/{batchId}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  @Override
  public @NonNull ResponseEntity<@NonNull FeedstockBatchResponse> update(
      @PathVariable @NonNull UUID batchId,
      @RequestBody @NonNull UpdateFeedstockBatchRequest request
  ) {
    final var updated = updateFeedstockBatchUseCase.update(
        FeedstockBatchRestMapper.toCommand(FeedstockBatchId.of(batchId), request)
    );

    return ResponseEntity.ok(FeedstockBatchRestMapper.toResponse(updated));
  }

  @PatchMapping(
      value = "/feedstock-batches/{batchId}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  @Override
  public @NonNull ResponseEntity<@NonNull FeedstockBatchResponse> patch(
      @PathVariable @NonNull UUID batchId,
      @RequestBody @NonNull PatchFeedstockBatchRequest request
  ) {
    final var updated = updateFeedstockBatchUseCase.update(
        FeedstockBatchRestMapper.toCommand(FeedstockBatchId.of(batchId), request)
    );

    return ResponseEntity.ok(FeedstockBatchRestMapper.toResponse(updated));
  }

  @DeleteMapping(value = "/feedstock-batches/{batchId}")
  @Override
  public @NonNull ResponseEntity<Void> delete(
      @PathVariable @NonNull UUID batchId,
      @RequestParam @NonNull UUID ciderMakerId
  ) {
    deleteFeedstockBatchUseCase.delete(new DeleteFeedstockBatchCommand(
        FeedstockBatchId.of(batchId),
        CiderMakerId.of(ciderMakerId)
    ));

    return ResponseEntity.noContent().build();
  }

  private static @Nullable FeedstockSeason seasonOf(
      @Nullable LocalDate startsOn,
      @Nullable LocalDate endsOn
  ) {
    if (startsOn == null && endsOn == null) {
      return null;
    }

    if (startsOn == null || endsOn == null) {
      throw new IllegalArgumentException("both seasonStartsOn and seasonEndsOn must be provided");
    }

    return FeedstockSeason.of(startsOn, endsOn);
  }
}
