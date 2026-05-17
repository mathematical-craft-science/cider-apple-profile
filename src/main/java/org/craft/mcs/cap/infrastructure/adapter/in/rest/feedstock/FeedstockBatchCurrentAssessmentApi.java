package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchProfileTargetScoreBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.OptimizeFeedstockBatchCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.OptimizeFeedstockBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.ScoreFeedstockBatchProfileTargetRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Feedstock batch current assessments",
    description = "Assessment, graph, target scoring and composition optimization endpoints based on the latest measurement of each feedstock batch."
)
public interface FeedstockBatchCurrentAssessmentApi {

  @Operation(
      summary = "Assess current feedstock batch sugar-acidity zone",
      description = "Uses the latest feedstock measurement in the batch and maps its chemical profile to a sugar-acidity graph zone when possible."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Current sugar-acidity assessment or unplottable result.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchCurrentSugarAcidityZoneResponse.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch or current measurement not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentSugarAcidityZoneResponse> assessSugarAcidityZone(
      @Parameter(description = "Feedstock batch identifier.", schema = @Schema(format = "uuid"))
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
  @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentSugarAcidityZonePointBatchResponse> listSugarAcidityZonePoints(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Current feedstock graph point query.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchCurrentSugarAcidityZonePointQueryRequest.class))
      )
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQueryRequest request
  );

  @Operation(
      summary = "Predict current feedstock batch potential ABV",
      description = "Predicts potential ABV from the latest batch measurement sugar value."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Current potential ABV prediction.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchCurrentPotentialAbvResponse.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Current profile has no sugar measurement.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentPotentialAbvResponse> predictPotentialAbv(
      @Parameter(description = "Feedstock batch identifier.", schema = @Schema(format = "uuid"))
      @NonNull UUID batchId
  );

  @Operation(
      summary = "Score current feedstock batches against profile target",
      description = "Scores candidate feedstock batches by distance from a predefined or custom profile target."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Target distance scores.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchProfileTargetScoreBatchResponse.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchProfileTargetScoreBatchResponse> scoreProfileTarget(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Target scoring request.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScoreFeedstockBatchProfileTargetRequest.class))
      )
      @NonNull ScoreFeedstockBatchProfileTargetRequest request
  );

  @Operation(
      summary = "Optimize feedstock batch composition",
      description = "Builds candidate source compositions from current feedstock batches and ranks them by distance to target."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Composition optimization result.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = OptimizeFeedstockBatchCompositionResponse.class))
  )
  @NonNull ResponseEntity<@NonNull OptimizeFeedstockBatchCompositionResponse> optimizeComposition(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Composition optimization request.",
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = OptimizeFeedstockBatchCompositionRequest.class))
      )
      @NonNull OptimizeFeedstockBatchCompositionRequest request
  );
}
