package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZoneResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Apple variety sugar-acidity analysis",
    description = "Sugar-acidity graph endpoints for apple varieties."
)
public interface AppleVarietySugarAcidityApi {

  @Operation(
      summary = "Assess apple variety sugar-acidity zone",
      description = "Maps an apple variety chemical profile to a Jolicoeur sugar-acidity graph zone when possible."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Sugar-acidity zone assessment or unplottable result.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AppleVarietySugarAcidityZoneResponse.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Apple variety not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AppleVarietySugarAcidityZoneResponse> assessSugarAcidityZone(
      @Parameter(description = "Apple variety identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID varietyId
  );

  @Operation(
      summary = "List apple variety sugar-acidity graph points",
      description = "Returns plottable and unplottable apple variety points for sugar-acidity graph rendering."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Batch of apple variety graph points.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AppleVarietySugarAcidityZonePointBatchResponse.class))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid graph point query.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AppleVarietySugarAcidityZonePointBatchResponse> listSugarAcidityZonePoints(
      @RequestBody(
          description = "Apple variety graph point query.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AppleVarietySugarAcidityZonePointQueryRequest.class))
      )
      @NonNull AppleVarietySugarAcidityZonePointQueryRequest request
  );
}
