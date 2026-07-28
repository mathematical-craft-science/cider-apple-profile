package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.QueryFeedstockPlanningBatchesUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceBatchQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.mapper.FeedstockWorkspaceQueryRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.mapper.FeedstockWorkspaceResponseRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class FeedstockWorkspaceController implements FeedstockWorkspaceApi {

  private final @NonNull QueryFeedstockPlanningBatchesUseCase queryFeedstockPlanningBatchesUseCase;

  @Override
  @PostMapping(
      value = "/feedstock-workspace/batches",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull FeedstockWorkspaceResponse> batches(
      @RequestBody @NonNull FeedstockWorkspaceBatchQueryRequest request
  ) {
    final var view = queryFeedstockPlanningBatchesUseCase.query(
        FeedstockWorkspaceQueryRestMapper.toQuery(request)
    );

    return ResponseEntity.ok(FeedstockWorkspaceResponseRestMapper.toResponse(view));
  }
}
