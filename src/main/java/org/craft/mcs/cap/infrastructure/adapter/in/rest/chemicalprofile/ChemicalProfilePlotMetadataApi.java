package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityGraphMetadataResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Chemical profiles", description = "Metadata and plotting contracts for chemical profiles.")
public interface ChemicalProfilePlotMetadataApi {

  @Operation(
      summary = "Get sugar-acidity plot metadata",
      description = "Returns axis definitions, threshold lines and zone shapes for rendering the sugar-acidity graph."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Sugar-acidity graph metadata.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = SugarAcidityGraphMetadataResponse.class))
  )
  @NonNull ResponseEntity<@NonNull SugarAcidityGraphMetadataResponse> sugarAcidityPlotMetadata();
}
