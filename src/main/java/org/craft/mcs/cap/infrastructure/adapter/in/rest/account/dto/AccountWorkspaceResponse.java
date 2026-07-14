package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceStatus;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.jspecify.annotations.NonNull;

/** Transport representation of a CAP-owned product workspace. */
@Schema(description = "CAP product workspace and business-data ownership scope.")
public record AccountWorkspaceResponse(
    @Schema(
        description = "Stable CAP identifier of the collaboration and future data-ownership scope.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @Schema(
        description = "Human-readable workspace name.",
        example = "North Orchard Cidery",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String name,
    @Schema(
        description = "Business category of the CAP workspace.",
        implementation = WorkspaceType.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull WorkspaceType type,
    @Schema(
        description = "Lifecycle state controlling whether the workspace is selectable.",
        implementation = WorkspaceStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull WorkspaceStatus status
) {
}
