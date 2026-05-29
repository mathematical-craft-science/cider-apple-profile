package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FeedstockBatchCurrentFermentationStartReadinessResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Feedstock readiness analysis",
    description = "Fermentation-start readiness endpoints based on the latest measurement of a feedstock batch."
)
public interface FeedstockBatchReadinessApi {

  @Operation(
      summary = "Assess current feedstock batch fermentation-start readiness",
      description = "Assesses fermentation-start readiness from the latest batch measurement using sugar, pH and YAN."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Current fermentation-start readiness assessment.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchCurrentFermentationStartReadinessResponse.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Feedstock batch has no current measurement.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentFermentationStartReadinessResponse>
      assessFermentationStartReadiness(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId
  );
}
