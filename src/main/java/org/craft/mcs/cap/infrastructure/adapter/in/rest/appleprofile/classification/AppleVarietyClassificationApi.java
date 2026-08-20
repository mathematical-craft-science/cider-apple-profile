package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.dto.AppleVarietyClassificationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.dto.AppleVarietyNorthAmericanResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Apple variety classification", description = "Classification endpoints for apple varieties.")
public interface AppleVarietyClassificationApi {

  @Operation(
      summary = "Classify apple variety",
      description = "Classifies an apple variety using the requested cider classification system."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Apple variety classification result.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AppleVarietyClassificationResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Apple variety not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AppleVarietyClassificationResponse> classify(
      @Parameter(description = "Apple variety identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID varietyId,
      @Parameter(
          description = "Classification system to apply.",
          required = true,
          schema = @Schema(implementation = AppleVarietyClassificationSystem.class)
      )
      @NonNull AppleVarietyClassificationSystem system
  );

  @Operation(
      summary = "Classify apple variety by North American profile",
      description = "Returns North American sugar, acidity and tannin profile terms for an apple variety."
  )
  @ApiResponse(
      responseCode = "200",
      description = "North American profile result.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AppleVarietyNorthAmericanResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Apple variety reference profile lacks Specific Gravity, Titratable Acidity or tannin.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Apple variety not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AppleVarietyNorthAmericanResponse> classifyNorthAmerican(
      @Parameter(description = "Apple variety identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID varietyId
  );
}
