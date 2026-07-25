package org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
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
        description = "Kind of immutable trace fact represented by this step.",
        implementation = BatchTraceStepType.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BatchTraceStepType stepType,
    @Schema(
        description = "Business time when the trace fact occurred.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant occurredAt,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Input material states consumed or referenced by the trace step.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = BatchTraceStepSubjectResponse.class)
    )
    @NonNull List<@NonNull BatchTraceStepSubjectResponse> inputs,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Output material states produced by the trace step.",
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
