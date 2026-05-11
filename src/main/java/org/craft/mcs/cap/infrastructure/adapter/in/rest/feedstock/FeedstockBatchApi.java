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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
  @NonNull ResponseEntity<@NonNull FeedstockBatchListResponse> list(
      @Parameter(description = "Cider maker identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID ciderMakerId,
      @Parameter(description = "Optional season filter start date.", schema = @Schema(format = "date"))
      @Nullable LocalDate seasonStartsOn,
      @Parameter(description = "Optional season filter end date.", schema = @Schema(format = "date"))
      @Nullable LocalDate seasonEndsOn
  );
}
