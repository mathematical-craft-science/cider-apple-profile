package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockSampleRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Feedstock samples", description = "Feedstock sample creation and read-model endpoints.")
public interface FeedstockSampleApi {

  @Operation(
      summary = "Create feedstock sample",
      description = "Creates a feedstock sample inside an existing batch. The cider maker is derived from the batch.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Feedstock sample creation payload.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateFeedstockSampleRequest.class))
      )
  )
  @ApiResponse(
      responseCode = "201",
      description = "Feedstock sample created.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockSampleResponse.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockSampleResponse> create(
      @NonNull CreateFeedstockSampleRequest request
  );

  @Operation(
      summary = "List feedstock samples by batch",
      description = "Returns feedstock sample summaries for a single feedstock batch."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock sample list.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockSampleListResponse.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockSampleListResponse> listByBatchId(
      @Parameter(description = "Feedstock batch identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID batchId
  );
}
