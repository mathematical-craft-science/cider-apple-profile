package org.craft.mcs.cap.infrastructure.adapter.in.rest.production;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchCandidateRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchCandidateResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Production batch candidates",
    description = "Assessment and persistence of production scenarios before explicit production confirmation."
)
public interface ProductionBatchCandidateApi {

  @Operation(
      summary = "Create production batch candidate",
      description = "Reads authoritative current source measurements, calculates the composition and optional target "
          + "assessment, then saves an immutable candidate snapshot without consuming source volume."
  )
  @ApiResponse(
      responseCode = "201",
      description = "Production candidate created.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProductionBatchCandidateResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "At least one source belongs to another workspace.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "A referenced production source was not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "A source profile is unavailable or selected volume exceeds currently available volume.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ProductionBatchCandidateResponse create(
      @RequestBody(
          description = "Production candidate assessment request.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CreateProductionBatchCandidateRequest.class)
          )
      )
      @NonNull CreateProductionBatchCandidateRequest request
  );

  @Operation(
      summary = "Get production batch candidate",
      description = "Returns the saved source composition, calculated profile, warnings and optional target "
          + "assessment used for production confirmation."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Production candidate.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProductionBatchCandidateResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Production candidate not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "403",
      description = "Production candidate belongs to an inaccessible workspace.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ProductionBatchCandidateResponse get(
      @Parameter(
          description = "Production candidate identifier.",
          required = true,
          schema = @Schema(format = "uuid")
      )
      @NonNull UUID candidateId
  );
}
