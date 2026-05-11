package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Feedstock measurements", description = "Feedstock measurement creation and read-model endpoints.")
public interface FeedstockMeasurementApi {

  @Operation(
      summary = "Create feedstock measurement",
      description = "Creates a feedstock measurement inside an existing batch. The cider maker is derived from the batch.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Feedstock measurement creation payload.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateFeedstockMeasurementRequest.class))
      )
  )
  @ApiResponse(
      responseCode = "201",
      description = "Feedstock measurement created.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockMeasurementResponse.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementResponse> create(
      @Parameter(description = "Feedstock batch identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @NonNull CreateFeedstockMeasurementRequest request
  );

  @Operation(
      summary = "List feedstock measurements by batch",
      description = "Returns feedstock measurement summaries for a single feedstock batch."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock measurement list.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockMeasurementListResponse.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementListResponse> listByBatchId(
      @Parameter(description = "Feedstock batch identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID batchId
  );
}
