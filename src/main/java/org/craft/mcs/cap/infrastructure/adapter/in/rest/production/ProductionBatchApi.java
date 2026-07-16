package org.craft.mcs.cap.infrastructure.adapter.in.rest.production;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.BatchTraceabilityGraphResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchDetailsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Production batches",
    description = "Production batch workspace endpoints: list, details, traceability and creation."
)
public interface ProductionBatchApi {

  @Operation(
      summary = "List production batches",
      description = "Returns production batches owned by a workspace. The response intentionally does not include "
          + "traceability graph data."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Production batch list.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProductionBatchListResponse.class)
      )
  )
  @NonNull ProductionBatchListResponse list(
      @Parameter(description = "Workspace identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID workspaceId
  );

  @Operation(
      summary = "Get production batch",
      description = "Returns production batch details without source traceability graph. Use the traceability endpoint "
          + "to inspect material origin."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Production batch details.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProductionBatchDetailsResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Production batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ProductionBatchDetailsResponse get(
      @Parameter(description = "Production batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId
  );

  @Operation(
      summary = "Get production batch traceability graph",
      description = "Returns a source traceability graph for the production batch. The graph is a separate read model "
          + "and is not embedded into batch details."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Production batch traceability graph.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = BatchTraceabilityGraphResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Production batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull BatchTraceabilityGraphResponse traceability(
      @Parameter(description = "Production batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId
  );

  @Operation(
      summary = "Create production batch",
      description = "Confirms a saved production candidate, consumes its source volumes, creates the production batch "
          + "and appends traceability atomically. The candidate is rejected when source evidence has changed."
  )
  @ApiResponse(
      responseCode = "201",
      description = "Production batch created.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProductionBatchResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid production batch request.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "403",
      description = "At least one feedstock source belongs to another workspace.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Production candidate or one of its sources was not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "The candidate was already confirmed, became stale or a resource was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Requested source volume exceeds available feedstock volume or its current profile is unavailable.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ProductionBatchResponse create(
      @RequestBody(
          description = "Production batch creation request.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CreateProductionBatchRequest.class)
          )
      )
      @NonNull CreateProductionBatchRequest request
  );
}
