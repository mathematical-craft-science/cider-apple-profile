package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchGraphPointResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Feedstock sugar-acidity analysis",
    description = "Sugar-acidity graph endpoints based on the latest measurement of each feedstock batch."
)
public interface FeedstockBatchSugarAcidityApi {

  @Operation(
      summary = "Assess current feedstock batch sugar-acidity zone",
      description = "Uses the latest feedstock measurement in the batch and maps its chemical profile to a sugar-acidity graph zone when possible."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Current sugar-acidity assessment or unplottable result.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchGraphPointResponse.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch or current measurement not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchGraphPointResponse> assessSugarAcidityZone(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId
  );

  @Operation(
      summary = "List current feedstock batch sugar-acidity graph points",
      description = "Returns plottable and unplottable graph points using only the latest measurement per feedstock batch."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Batch of current feedstock graph points.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchCurrentSugarAcidityZonePointBatchResponse.class))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid current feedstock graph point query.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentSugarAcidityZonePointBatchResponse> listSugarAcidityZonePoints(
      @RequestBody(
          description = "Current feedstock graph point query.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchCurrentSugarAcidityZonePointQueryRequest.class))
      )
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQueryRequest request
  );
}
