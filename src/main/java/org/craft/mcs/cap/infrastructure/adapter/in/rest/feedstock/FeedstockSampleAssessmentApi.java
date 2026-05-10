package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSamplePotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZoneResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Feedstock assessments", description = "Feedstock sample graph assessment and ABV prediction endpoints.")
public interface FeedstockSampleAssessmentApi {

  @Operation(
      summary = "Assess feedstock sample sugar-acidity zone",
      description = "Maps a feedstock sample chemical profile to a sugar-acidity graph zone when enough data exists."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Sugar-acidity zone assessment or unplottable result.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockSampleSugarAcidityZoneResponse.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockSampleSugarAcidityZoneResponse> assessSugarAcidityZone(
      @Parameter(description = "Feedstock sample identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID sampleId
  );

  @Operation(
      summary = "List feedstock sample sugar-acidity graph points",
      description = "Returns plottable and unplottable feedstock sample points for a cider maker."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Batch of feedstock sample graph points.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockSampleSugarAcidityZonePointBatchResponse.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockSampleSugarAcidityZonePointBatchResponse> listSugarAcidityZonePoints(
      @NonNull FeedstockSampleSugarAcidityZonePointQueryRequest request
  );

  @Operation(
      summary = "Predict feedstock sample potential ABV",
      description = "Predicts potential ABV from the sample sugar measurement using the configured ABV prediction strategy."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Potential ABV prediction.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockSamplePotentialAbvResponse.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Chemical profile has no sugar measurement.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockSamplePotentialAbvResponse> predictPotentialAbv(
      @Parameter(description = "Feedstock sample identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID sampleId
  );
}
