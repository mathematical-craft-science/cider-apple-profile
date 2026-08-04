package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceBatchQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Feedstock Workspace",
    description = "Workspace read models for the feedstock planning workspace."
)
public interface FeedstockWorkspaceApi {

  @Operation(
      summary = "Get Feedstock Workspace batches",
      description = "Returns batch metadata enriched with a state-aware current profile, graph projection, ABV, "
          + "YAN, readiness and optional target-aware candidate data.",
      requestBody = @RequestBody(
          description = "Workspace query. Include target to enrich batches with target distance and contribution role.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = FeedstockWorkspaceBatchQueryRequest.class)
          )
      )
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock Workspace read model.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockWorkspaceResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid workspace query.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockWorkspaceResponse> batches(
      @NonNull FeedstockWorkspaceBatchQueryRequest request
  );
}
