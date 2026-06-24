package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto.MeasurementUnitsMetadataResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;

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
      description = "Returns CAP canonical unit labels and accepted value ranges for configuring numeric UI controls."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Canonical measurement unit metadata.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = MeasurementUnitsMetadataResponse.class)
      )
  )
  @NonNull ResponseEntity<@NonNull MeasurementUnitsMetadataResponse> metadata();
}
