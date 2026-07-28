package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.abv;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.abv.dto.AppleVarietyPotentialAbvResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Apple variety ABV analysis",
    description = "Potential ABV endpoints for apple variety reference profiles."
)
public interface AppleVarietyPotentialAbvApi {

  @Operation(
      summary = "Predict apple variety potential ABV",
      description = """
          Predicts potential ABV from the apple variety reference SG using the configured ABV
          prediction strategy.
          """
  )
  @ApiResponse(
      responseCode = "200",
      description = "Potential ABV prediction.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AppleVarietyPotentialAbvResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Chemical profile does not contain sugar.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Apple variety not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AppleVarietyPotentialAbvResponse> predictPotentialAbv(
      @Parameter(description = "Apple variety identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID varietyId
  );
}
