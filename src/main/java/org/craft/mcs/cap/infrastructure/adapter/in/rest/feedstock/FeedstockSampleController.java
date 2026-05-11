package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockSampleUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.ListFeedstockSamplesUseCase;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockSampleRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper.FeedstockSampleRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class FeedstockSampleController implements FeedstockSampleApi {

  private final @NonNull CreateFeedstockSampleUseCase createFeedstockSampleUseCase;
  private final @NonNull ListFeedstockSamplesUseCase listFeedstockSamplesUseCase;

  @Override
  @PostMapping(value = "/feedstock-samples", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockSampleResponse> create(
      @RequestBody @NonNull CreateFeedstockSampleRequest request
  ) {
    final var created = createFeedstockSampleUseCase.create(
        FeedstockSampleRestMapper.toCommand(request)
    );

    return ResponseEntity
        .created(URI.create("/feedstock-samples/" + created.sampleId().value()))
        .body(FeedstockSampleRestMapper.toResponse(created));
  }

  @Override
  @GetMapping(value = "/feedstock-samples", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockSampleListResponse> listByBatchId(
      @RequestParam @NonNull UUID batchId
  ) {
    final var samples = listFeedstockSamplesUseCase.listByBatchId(FeedstockBatchId.of(batchId));

    return ResponseEntity.ok(new FeedstockSampleListResponse(
        samples.stream()
            .map(FeedstockSampleRestMapper::toResponse)
            .toList()
    ));
  }
}
