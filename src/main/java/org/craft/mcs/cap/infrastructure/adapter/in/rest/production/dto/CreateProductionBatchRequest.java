package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.FORMAT;
import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.PATTERN;


@Schema(description = "Request to confirm a saved production candidate as a production batch.")
public record CreateProductionBatchRequest(
    @Schema(
        description = "Workspace identifier.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull UUID workspaceId,
    @Schema(
        description = "Production batch name.",
        example = "October cider base",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank @NonNull String name,
    @Schema(
        description = "UUIDv7 identifier of the operational vessel that will hold the production batch.",
        format = FORMAT,
        pattern = PATTERN,
        example = "019fd42c-3ac8-7000-91fe-48173dfc0c7e",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull UUID vesselId,
    @Schema(
        description = "Saved production candidate identifier.",
        format = "uuid",
        example = "019f0f03-4715-7cbd-a634-a178df719e28",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull UUID candidateId,
    @Schema(
        description = "Optional production note.",
        example = "Planned first fermentation in tank A.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String note
) {
}
