package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.jspecify.annotations.NonNull;

/** Request replacing editable workspace metadata. */
@Schema(description = "Editable CAP workspace metadata.")
public record UpdateWorkspaceRequest(
    @Schema(example = "North Orchard Cidery", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @NonNull String name,
    @Schema(implementation = WorkspaceType.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @NonNull WorkspaceType type
) {
}
