package org.craft.mcs.cap.infrastructure.adapter.in.rest.production;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Production batches", description = "Production batch creation endpoints.")
public interface ProductionBatchApi {

  @Operation(
      summary = "Create production batch",
      description = "Creates a production batch from one or more feedstock batch sources and reserves/consumes source volume atomically."
  )
  @ApiResponse(
      responseCode = "201",
      description = "Production batch created.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductionBatchResponse.class))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid production batch request.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "403",
      description = "At least one feedstock source belongs to another cider maker.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock source not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "A source or production resource was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Requested source volume exceeds available feedstock volume.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ProductionBatchResponse create(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Production batch creation request.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateProductionBatchRequest.class))
      )
      @NonNull CreateProductionBatchRequest request
  );
}
