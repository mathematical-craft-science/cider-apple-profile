package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.dto.FeedstockMeasurementPotentialAbvResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Feedstock measurement ABV analysis",
    description = "Potential ABV endpoints for historical feedstock measurements."
)
public interface FeedstockMeasurementPotentialAbvApi {

  @Operation(
      summary = "Predict feedstock measurement potential ABV",
      description = "Predicts potential ABV from one historical measurement sugar value."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Potential ABV prediction.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockMeasurementPotentialAbvResponse.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Measurement profile has no sugar measurement.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementPotentialAbvResponse> predictPotentialAbv(
      @Parameter(description = "Feedstock measurement identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID measurementId
  );
}
