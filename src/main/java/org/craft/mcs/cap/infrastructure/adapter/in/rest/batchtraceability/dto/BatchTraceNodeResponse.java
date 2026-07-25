package org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Batch traceability graph node.")
public record BatchTraceNodeResponse(
    @Schema(
        description = "Stable identifier of this immutable historical state in the traceability graph.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID traceStateId,
    @Schema(
        description = "Business material identity represented by this historical state.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BatchTraceSubjectResponse subject,
    @Schema(
        description = "Immutable state snapshot captured by the step that produced this node.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BatchTraceMaterialSnapshotResponse snapshot
) {
}
