package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Batch traceability graph node.")
public record BatchTraceNodeResponse(
    @Schema(description = "Trace subject represented by this node.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BatchTraceSubjectResponse subject,
    @Schema(
        description = "Material snapshot captured for this subject when the trace step was recorded.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BatchTraceMaterialSnapshotResponse snapshot
) {
}
