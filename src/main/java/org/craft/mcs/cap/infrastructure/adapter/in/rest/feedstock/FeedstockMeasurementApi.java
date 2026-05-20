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
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.PatchFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.UpdateFeedstockMeasurementRequest;
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
      responseCode = "400",
      description = "Invalid measurement payload, for example a measurement date in the future.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
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

  @Operation(
      summary = "Replace feedstock measurement",
      description = "Replaces editable feedstock measurement fields and the whole chemical profile."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock measurement updated.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockMeasurementResponse.class))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid measurement payload, for example a measurement date in the future.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch or measurement not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock measurement was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementResponse> update(
      @Parameter(description = "Feedstock batch identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @Parameter(description = "Feedstock measurement identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID measurementId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Full feedstock measurement update payload.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdateFeedstockMeasurementRequest.class))
      )
      @NonNull UpdateFeedstockMeasurementRequest request
  );

  @Operation(
      summary = "Patch feedstock measurement",
      description = "Partially updates measurement metadata and merges provided chemical profile axes into the existing profile."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock measurement patched.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockMeasurementResponse.class))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid measurement payload, for example a measurement date in the future.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch or measurement not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock measurement was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementResponse> patch(
      @Parameter(description = "Feedstock batch identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @Parameter(description = "Feedstock measurement identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID measurementId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Partial feedstock measurement update payload.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = PatchFeedstockMeasurementRequest.class))
      )
      @NonNull PatchFeedstockMeasurementRequest request
  );

  @Operation(
      summary = "Delete feedstock measurement",
      description = "Deletes a feedstock measurement from a batch."
  )
  @ApiResponse(responseCode = "204", description = "Feedstock measurement deleted.")
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch or measurement not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock measurement was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<Void> delete(
      @Parameter(description = "Feedstock batch identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @Parameter(description = "Feedstock measurement identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID measurementId
  );
}
