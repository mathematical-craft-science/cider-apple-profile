package org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Trace step subject with optional material snapshot.")
public record BatchTraceStepSubjectResponse(
    @Schema(
        description = "Identifier of the immutable historical state consumed or produced by the operation.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID traceStateId,
    @Schema(
        description = "Stable business material identity participating in the operation.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BatchTraceSubjectResponse subject,
    @Schema(
        description = "Material snapshot captured for this subject at operation time.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BatchTraceMaterialSnapshotResponse snapshot
) {
}
