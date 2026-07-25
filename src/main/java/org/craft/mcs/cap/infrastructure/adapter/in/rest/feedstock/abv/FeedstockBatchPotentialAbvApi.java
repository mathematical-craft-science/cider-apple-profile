package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.dto.FeedstockBatchCurrentPotentialAbvResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Feedstock ABV analysis",
    description = "Potential ABV endpoints based on the state-aware current feedstock profile."
)
public interface FeedstockBatchPotentialAbvApi {

  @Operation(
      summary = "Predict current feedstock batch potential ABV",
      description = "Predicts potential ABV from the sugar evidence selected for the state-aware current profile."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Current potential ABV prediction.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockBatchCurrentPotentialAbvResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Current profile has no sugar measurement.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentPotentialAbvResponse> predictPotentialAbv(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId
  );
}
