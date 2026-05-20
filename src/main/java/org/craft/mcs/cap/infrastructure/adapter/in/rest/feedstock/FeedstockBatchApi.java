package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.PatchFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.UpdateFeedstockBatchRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Feedstock batches", description = "Feedstock batch creation and lookup endpoints.")
public interface FeedstockBatchApi {

  @Operation(
      summary = "Create feedstock batch",
      description = "Creates a feedstock batch with season dates, optional source and available volume.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Feedstock batch creation payload.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateFeedstockBatchRequest.class))
      )
  )
  @ApiResponse(
      responseCode = "201",
      description = "Feedstock batch created.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchResponse.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchResponse> create(
      @NonNull CreateFeedstockBatchRequest request
  );

  @Operation(
      summary = "List feedstock batches",
      description = "Lists feedstock batches by cider maker, optionally filtered by an overlapping season date range."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock batch list.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchListResponse.class))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid season filter, for example only one season boundary was provided.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchListResponse> list(
      @Parameter(description = "Cider maker identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID ciderMakerId,
      @Parameter(description = "Optional season filter start date.", schema = @Schema(format = "date"))
      @Nullable LocalDate seasonStartsOn,
      @Parameter(description = "Optional season filter end date.", schema = @Schema(format = "date"))
      @Nullable LocalDate seasonEndsOn
  );

  @Operation(
      summary = "Replace feedstock batch",
      description = "Replaces editable feedstock batch fields. The request must include the cider maker owner id for ownership validation.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Full feedstock batch update payload.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdateFeedstockBatchRequest.class))
      )
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock batch updated.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchResponse.class))
  )
  @ApiResponse(
      responseCode = "403",
      description = "Batch belongs to another cider maker.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock batch was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchResponse> update(
      @Parameter(description = "Feedstock batch identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @NonNull UpdateFeedstockBatchRequest request
  );

  @Operation(
      summary = "Patch feedstock batch",
      description = "Partially updates editable feedstock batch fields. Blank optional text values clear the field."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock batch patched.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchResponse.class))
  )
  @ApiResponse(
      responseCode = "403",
      description = "Batch belongs to another cider maker.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock batch was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchResponse> patch(
      @Parameter(description = "Feedstock batch identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Partial feedstock batch update payload.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = PatchFeedstockBatchRequest.class))
      )
      @NonNull PatchFeedstockBatchRequest request
  );

  @Operation(
      summary = "Delete feedstock batch",
      description = "Soft-deletes a feedstock batch. The batch disappears from normal workspace queries while historical rows remain in persistence."
  )
  @ApiResponse(responseCode = "204", description = "Feedstock batch deleted.")
  @ApiResponse(
      responseCode = "403",
      description = "Batch belongs to another cider maker.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock batch was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<Void> delete(
      @Parameter(description = "Feedstock batch identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @Parameter(description = "Cider maker identifier used for ownership validation.", schema = @Schema(format = "uuid"))
      @NonNull UUID ciderMakerId
  );
}
