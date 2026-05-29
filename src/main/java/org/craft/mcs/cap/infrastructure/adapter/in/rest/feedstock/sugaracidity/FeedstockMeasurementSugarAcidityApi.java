package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZoneResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Feedstock measurement sugar-acidity analysis",
    description = "Sugar-acidity graph endpoints for historical feedstock measurements."
)
public interface FeedstockMeasurementSugarAcidityApi {

  @Operation(
      summary = "Assess feedstock measurement sugar-acidity zone",
      description = "Maps one historical feedstock measurement to a sugar-acidity graph zone when possible."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Sugar-acidity assessment or unplottable result.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockMeasurementSugarAcidityZoneResponse.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock measurement not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementSugarAcidityZoneResponse> assessSugarAcidityZone(
      @Parameter(description = "Feedstock measurement identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID measurementId
  );

  @Operation(
      summary = "List feedstock measurement sugar-acidity graph points",
      description = "Returns plottable and unplottable historical feedstock measurement points."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Batch of measurement graph points.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockMeasurementSugarAcidityZonePointBatchResponse.class))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid measurement graph point query.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementSugarAcidityZonePointBatchResponse> listSugarAcidityZonePoints(
      @RequestBody(
          description = "Historical measurement graph point query.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockMeasurementSugarAcidityZonePointQueryRequest.class))
      )
      @NonNull FeedstockMeasurementSugarAcidityZonePointQueryRequest request
  );
}
