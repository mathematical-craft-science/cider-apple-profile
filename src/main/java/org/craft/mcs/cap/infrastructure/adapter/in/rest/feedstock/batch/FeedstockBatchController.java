package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.CreateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.DeleteFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.DeleteFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.ListFeedstockBatchesQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.UpdateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.detail.port.in.GetFeedstockBatchDetailQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.detail.port.in.GetFeedstockBatchDetailUseCase;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.CreateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.FeedstockBatchListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.FeedstockBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.PatchFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.UpdateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.mapper.FeedstockBatchRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.mapper.FeedstockBatchDetailRestMapper;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
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
  private final @NonNull GetFeedstockBatchDetailUseCase getFeedstockBatchDetailUseCase;
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
        .created(URI.create("/feedstock-batches/" + created.batch().batch().batchId().value()))
        .body(FeedstockBatchRestMapper.toResponse(created));
  }

  @Override
  @GetMapping(value = "/feedstock-batches", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockBatchListResponse> list(
      @RequestParam @NonNull UUID workspaceId,
      @RequestParam(required = false) @Nullable LocalDate seasonStartsOn,
      @RequestParam(required = false) @Nullable LocalDate seasonEndsOn
  ) {
    final var batches = listFeedstockBatchesUseCase.list(new ListFeedstockBatchesQuery(
        WorkspaceId.of(workspaceId),
        seasonOf(seasonStartsOn, seasonEndsOn)
    ));

    return ResponseEntity.ok(new FeedstockBatchListResponse(
        batches.stream()
            .map(FeedstockBatchRestMapper::toResponse)
            .toList()
    ));
  }

  @Override
  @GetMapping(value = "/feedstock-batches/{batchId}", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockBatchDetailResponse> get(
      @PathVariable @NonNull UUID batchId,
      @RequestParam @NonNull UUID workspaceId
  ) {
    final var detail = getFeedstockBatchDetailUseCase.get(new GetFeedstockBatchDetailQuery(
        FeedstockBatchId.of(batchId),
        WorkspaceId.of(workspaceId)
    ));

    return ResponseEntity.ok(FeedstockBatchDetailRestMapper.toResponse(detail));
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
      @RequestParam @NonNull UUID workspaceId
  ) {
    deleteFeedstockBatchUseCase.delete(new DeleteFeedstockBatchCommand(
        FeedstockBatchId.of(batchId),
        WorkspaceId.of(workspaceId)
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
      throw new ApplicationValidationException("both seasonStartsOn and seasonEndsOn must be provided");
    }

    return FeedstockSeason.of(startsOn, endsOn);
  }
}
