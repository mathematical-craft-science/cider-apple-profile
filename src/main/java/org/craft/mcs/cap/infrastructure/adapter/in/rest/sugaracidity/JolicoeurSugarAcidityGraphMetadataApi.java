package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.JolicoeurSugarAcidityGraphMetadataResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/** HTTP contract exposing metadata for the configured Jolicoeur sugar-acidity graph. */
@Tag(name = "Sugar-acidity graph", description = "Shared metadata for sugar-acidity interpretation.")
public interface JolicoeurSugarAcidityGraphMetadataApi {

  @Operation(
      summary = "Get Jolicoeur sugar-acidity graph metadata",
      description = "Returns the configured SG/TA axis extents, zone boundary values, and zone geometry."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Sugar-acidity graph metadata.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = JolicoeurSugarAcidityGraphMetadataResponse.class)
      )
  )
  @NonNull ResponseEntity<@NonNull JolicoeurSugarAcidityGraphMetadataResponse>
      jolicoeurSugarAcidityGraphMetadata();
}
