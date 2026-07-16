package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to confirm a saved production candidate as a production batch.")
public record CreateProductionBatchRequest(
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
        description = "Saved production candidate identifier.",
        format = "uuid",
        example = "019f0f03-4715-7cbd-a634-a178df719e28",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID candidateId,
    @Schema(
        description = "Optional production note.",
        example = "Planned first fermentation in tank A.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String note
) {
}
