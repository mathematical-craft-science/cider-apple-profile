package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculatedCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeCompositionResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Composition analysis",
    description = "Calculates and optimizes weighted source compositions."
)
public interface CompositionApi {

  @Operation(
      operationId = "calculateComposition",
      summary = "Calculate a source composition",
      description = "Calculates resulting characteristics and potential ABV from explicitly weighted sources."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Calculated composition.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = CalculatedCompositionResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid composition request or unsupported strategy selector.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Composition input is syntactically valid but cannot be processed, for example because "
          + "required axes or target data are missing.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull CalculatedCompositionResponse> calculate(
      @RequestBody(
          description = "Composition calculation request.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CalculateCompositionRequest.class)
          )
      )
      @NonNull CalculateCompositionRequest request
  );

  @Operation(
      operationId = "optimizeComposition",
      summary = "Optimize a source composition",
      description = "Searches candidate source weights and ranks compositions by the complete objective score. "
          + "Target distance is the base score; optional readiness criteria can add penalties."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Composition optimization result.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = OptimizeCompositionResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid optimization request or unsupported strategy selector.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Optimization input is syntactically valid but cannot be processed, for example because "
          + "sources or target data are insufficient.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull OptimizeCompositionResponse> optimize(
      @RequestBody(
          description = "Composition optimization request.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = OptimizeCompositionRequest.class)
          )
      )
      @NonNull OptimizeCompositionRequest request
  );
}
