package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietyClassificationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietyNorthAmericanResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZoneResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Apple varieties", description = "Classification and sugar-acidity graph endpoints for apple varieties.")
public interface AppleVarietyClassificationApi {

  @Operation(
      summary = "Classify apple variety",
      description = "Classifies an apple variety using the requested cider classification system."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Apple variety classification result.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AppleVarietyClassificationResponse.class))
  )
  @NonNull ResponseEntity<@NonNull AppleVarietyClassificationResponse> classify(
      @Parameter(description = "Apple variety identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID varietyId,
      @Parameter(description = "Classification system to apply.", schema = @Schema(implementation = AppleVarietyClassificationSystem.class))
      @NonNull AppleVarietyClassificationSystem system
  );

  @Operation(
      summary = "Classify apple variety by North American profile",
      description = "Returns North American sugar, acidity and tannin profile terms for an apple variety."
  )
  @ApiResponse(
      responseCode = "200",
      description = "North American profile result.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AppleVarietyNorthAmericanResponse.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Chemical profile does not contain all required axes.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AppleVarietyNorthAmericanResponse> classifyNorthAmerican(
      @Parameter(description = "Apple variety identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID varietyId
  );

  @Operation(
      summary = "Assess apple variety sugar-acidity zone",
      description = "Maps an apple variety chemical profile to a Jolicoeur sugar-acidity graph zone when possible."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Sugar-acidity zone assessment or unplottable result.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AppleVarietySugarAcidityZoneResponse.class))
  )
  @NonNull ResponseEntity<@NonNull AppleVarietySugarAcidityZoneResponse> assessSugarAcidityZone(
      @Parameter(description = "Apple variety identifier.", schema = @Schema(format = "uuid"))
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
  @NonNull ResponseEntity<@NonNull AppleVarietySugarAcidityZonePointBatchResponse> listSugarAcidityZonePoints(
      @NonNull AppleVarietySugarAcidityZonePointQueryRequest request
  );
}
