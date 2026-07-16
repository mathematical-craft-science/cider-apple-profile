package org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.jspecify.annotations.NonNull;


@Schema(description = "Batch traceability graph edge created from one trace step.")
public record BatchTraceEdgeResponse(
    @Schema(
        description = "Trace step identifier.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID stepId,
    @Schema(
        description = "Trace operation type.",
        implementation = BatchTraceOperationType.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BatchTraceOperationType operationType,
    @Schema(
        description = "Business time when the trace operation occurred.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant occurredAt,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Input subjects consumed or referenced by the operation.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = BatchTraceStepSubjectResponse.class)
    )
    @NonNull List<@NonNull BatchTraceStepSubjectResponse> inputs,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Output subjects produced or updated by the operation.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = BatchTraceStepSubjectResponse.class)
    )
    @NonNull List<@NonNull BatchTraceStepSubjectResponse> outputs
) {

  public BatchTraceEdgeResponse {
    inputs = List.copyOf(inputs);
    outputs = List.copyOf(outputs);
  }
}
