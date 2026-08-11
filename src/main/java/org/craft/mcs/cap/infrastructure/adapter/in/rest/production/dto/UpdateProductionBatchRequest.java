package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.FORMAT;
import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.PATTERN;


@Schema(description = "Request to replace editable production batch metadata.")
public record UpdateProductionBatchRequest(
    @Schema(
        description = "Workspace identifier used for ownership validation.",
        format = "uuid",
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
        description = "Initial vessel assignment. It can change only before the first post-creation operation.",
        format = FORMAT,
        pattern = PATTERN,
        example = "019fd42c-3ac8-7000-91fe-48173dfc0c7e",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull UUID vesselId,
    @Schema(
        description = "Optional production note. Blank value clears the note.",
        example = "Planned first fermentation in tank A.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String note
) {
}
