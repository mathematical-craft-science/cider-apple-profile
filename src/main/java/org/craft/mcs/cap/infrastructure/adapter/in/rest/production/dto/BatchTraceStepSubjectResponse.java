package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Trace step subject with optional material snapshot.")
public record BatchTraceStepSubjectResponse(
    @Schema(description = "Trace subject participating in the operation.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BatchTraceSubjectResponse subject,
    @Schema(
        description = "Material snapshot captured for this subject at operation time.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BatchTraceMaterialSnapshotResponse snapshot
) {
}
