package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Production batch response.")
public record ProductionBatchResponse(
    @Schema(
        description = "Production batch identifier.",
        format = "uuid",
        example = "8a9813cf-bc41-4d3f-82a9-51c5f00d4f87",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID batchId,
    @Schema(
        description = "Workspace identifier.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @Schema(
        description = "Production batch name.",
        example = "October cider base",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String name,
    @Schema(
        description = "Production batch kind.",
        implementation = ProductionBatchKind.class,
        example = "CIDER",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProductionBatchKind kind,
    @Schema(
        description = "Production batch status.",
        implementation = ProductionBatchStatus.class,
        example = "PLANNED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProductionBatchStatus status,
    @Schema(
        description = "Confirmed production candidate identifier.",
        format = "uuid",
        example = "019f0f03-4715-7cbd-a634-a178df719e28",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID candidateId,
    @Schema(description = "Accepted source composition snapshot.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProductionBatchCompositionResponse composition,
    @Schema(
        description = "Optional production note.",
        example = "Planned first fermentation in tank A.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String note
) {
}
