package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto.MeasurementUnitsMetadataResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.craft.mcs.cap.infrastructure.adapter.in.http.ActiveWorkspaceHttpHeaders.ACTIVE_WORKSPACE_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * OpenAPI-facing contract for the canonical unit metadata endpoint.
 *
 * <p>The endpoint gives clients stable transport metadata for numeric inputs without exposing
 * graph-specific thresholds, profile target rules, or profile analysis scenarios.
 */
@Tag(name = "Measurement units", description = "Canonical CAP measurement units and numeric input metadata.")
public interface MeasurementUnitMetadataApi {

  @Operation(
      summary = "Get measurement unit metadata",
      description = """
          Returns CAP canonical unit labels, accepted value ranges and measurement freshness
          metadata for configuring numeric UI controls and data-age indicators. Freshness metadata
          is read from persisted settings of the active workspace.
          """,
      parameters = @Parameter(
          name = ACTIVE_WORKSPACE_ID,
          description = "Active CAP workspace identifier for workspace-specific freshness metadata.",
          in = ParameterIn.HEADER,
          schema = @Schema(type = "string", format = "uuid")
      )
  )
  @ApiResponse(
      responseCode = "200",
      description = "Canonical measurement unit metadata.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = MeasurementUnitsMetadataResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "400",
      description = "The active workspace header is not a valid UUID.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "403",
      description = "The active workspace is not accessible to the current account.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "An explicit workspace selection is required for a multi-workspace account.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull MeasurementUnitsMetadataResponse> metadata();
}
