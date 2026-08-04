package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchProfileTargetScoreBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.OptimizeFeedstockBatchCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.OptimizeFeedstockBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.ScoreFeedstockBatchProfileTargetRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Feedstock composition planning",
    description = "Target scoring and composition optimization endpoints based on current feedstock batch profiles."
)
public interface FeedstockBatchCompositionPlanningApi {

  @Operation(
      summary = "Score current feedstock batches against profile target",
      description = "Scores candidate feedstock batches by distance from a predefined or custom profile target."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Target distance scores.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockBatchProfileTargetScoreBatchResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid target scoring request.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchProfileTargetScoreBatchResponse> scoreProfileTarget(
      @RequestBody(
          description = "Target scoring request.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ScoreFeedstockBatchProfileTargetRequest.class)
          )
      )
      @NonNull ScoreFeedstockBatchProfileTargetRequest request
  );

  @Operation(
      summary = "Optimize feedstock batch composition",
      description = """
          Builds candidate source compositions from current feedstock batches and ranks them by
          distance to target.
          """
  )
  @ApiResponse(
      responseCode = "200",
      description = "Composition optimization result.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = OptimizeFeedstockBatchCompositionResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid composition optimization request.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Optimization request is valid but cannot be solved with the selected sources or target.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull OptimizeFeedstockBatchCompositionResponse> optimizeComposition(
      @RequestBody(
          description = "Composition optimization request.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = OptimizeFeedstockBatchCompositionRequest.class)
          )
      )
      @NonNull OptimizeFeedstockBatchCompositionRequest request
  );
}
