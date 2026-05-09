package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockSampleUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockSampleRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper.FeedstockSampleRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public final class FeedstockSampleController {

  private final @NonNull CreateFeedstockSampleUseCase createFeedstockSampleUseCase;

  @PostMapping("/feedstock-samples")
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
}
