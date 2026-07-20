package org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Business object participating in batch traceability.")
public record BatchTraceSubjectResponse(
    @Schema(
        description = "Subject type.",
        implementation = BatchTraceSubjectType.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BatchTraceSubjectType type,
    @Schema(
        description = "Stable business subject identifier, shared by all historical states of that material. UUID "
            + "values are represented as strings because subject id format depends on subject type.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String id,
    @Schema(
        description = "Display name captured when the trace step was recorded.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String displayNameSnapshot
) {
}
