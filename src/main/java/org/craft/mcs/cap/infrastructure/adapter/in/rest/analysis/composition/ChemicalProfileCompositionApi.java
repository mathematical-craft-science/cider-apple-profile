package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.ChemicalProfileCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.OptimizeChemicalProfileCompositionResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Chemical profile compositions",
    description = "Generic chemical profile composition calculation and optimization endpoints."
)
public interface ChemicalProfileCompositionApi {

  @Operation(
      summary = "Calculate chemical profile composition",
      description = "Calculates the resulting chemical profile and potential ABV from explicitly provided sources "
          + "and weights."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Calculated chemical profile composition.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ChemicalProfileCompositionResponse.class)
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
  @NonNull ResponseEntity<@NonNull ChemicalProfileCompositionResponse> calculate(
      @RequestBody(
          description = "Composition calculation request.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CalculateChemicalProfileCompositionRequest.class)
          )
      )
      @NonNull CalculateChemicalProfileCompositionRequest request
  );

  @Operation(
      summary = "Optimize chemical profile composition",
      description = "Searches candidate source compositions and ranks them by target distance."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Chemical profile composition optimization result.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = OptimizeChemicalProfileCompositionResponse.class)
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
  @NonNull ResponseEntity<@NonNull OptimizeChemicalProfileCompositionResponse> optimize(
      @RequestBody(
          description = "Composition optimization request.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = OptimizeChemicalProfileCompositionRequest.class)
          )
      )
      @NonNull OptimizeChemicalProfileCompositionRequest request
  );
}
