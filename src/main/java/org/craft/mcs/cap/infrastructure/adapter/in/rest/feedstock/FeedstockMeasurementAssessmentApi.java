package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementSugarAcidityZoneResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Feedstock assessments", description = "Feedstock measurement graph assessment and ABV prediction endpoints.")
public interface FeedstockMeasurementAssessmentApi {

  @Operation(
      summary = "Assess feedstock measurement sugar-acidity zone",
      description = "Maps a feedstock measurement chemical profile to a sugar-acidity graph zone when enough data exists."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Sugar-acidity zone assessment or unplottable result.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockMeasurementSugarAcidityZoneResponse.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementSugarAcidityZoneResponse> assessSugarAcidityZone(
      @Parameter(description = "Feedstock measurement identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID measurementId
  );

  @Operation(
      summary = "List feedstock measurement sugar-acidity graph points",
      description = "Returns plottable and unplottable feedstock measurement points for a cider maker."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Batch of feedstock measurement graph points.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockMeasurementSugarAcidityZonePointBatchResponse.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementSugarAcidityZonePointBatchResponse> listSugarAcidityZonePoints(
      @NonNull FeedstockMeasurementSugarAcidityZonePointQueryRequest request
  );

  @Operation(
      summary = "Predict feedstock measurement potential ABV",
      description = "Predicts potential ABV from the measurement sugar value using the configured ABV prediction strategy."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Potential ABV prediction.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockMeasurementPotentialAbvResponse.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Chemical profile has no sugar measurement.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementPotentialAbvResponse> predictPotentialAbv(
      @Parameter(description = "Feedstock measurement identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID measurementId
  );
}
