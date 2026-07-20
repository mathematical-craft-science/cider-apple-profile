package org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto.BatchTraceabilityGraphResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** OpenAPI contract for querying traceability from a production batch subject. */
@Tag(name = "Batch traceability", description = "Material origin and transformation graph endpoints.")
public interface ProductionBatchTraceabilityApi {

  @Operation(
      summary = "Get production batch traceability graph",
      description = "Returns the traceability graph rooted at a production batch. Subject identifiers represent "
          + "stable business materials, while trace state identifiers distinguish their immutable historical states."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Production batch traceability graph.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = BatchTraceabilityGraphResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Traceability subject was not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull BatchTraceabilityGraphResponse traceability(
      @Parameter(description = "Production batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId
  );
}
