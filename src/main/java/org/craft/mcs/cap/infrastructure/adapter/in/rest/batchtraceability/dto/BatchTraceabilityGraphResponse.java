package org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Batch traceability graph response.")
public record BatchTraceabilityGraphResponse(
    @ArraySchema(
        arraySchema = @Schema(
            description = "Immutable historical material states participating in the traceability graph.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = BatchTraceNodeResponse.class)
    )
    @NonNull List<@NonNull BatchTraceNodeResponse> nodes,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Trace steps connecting input and output historical states.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = BatchTraceEdgeResponse.class)
    )
    @NonNull List<@NonNull BatchTraceEdgeResponse> edges
) {

  public BatchTraceabilityGraphResponse {
    nodes = List.copyOf(nodes);
    edges = List.copyOf(edges);
  }
}
