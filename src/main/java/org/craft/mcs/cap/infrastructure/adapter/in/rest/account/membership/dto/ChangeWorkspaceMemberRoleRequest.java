package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;


/** Request changing a member's non-owner workspace role. */
@Schema(description = "New non-owner role for an existing workspace member.")
public record ChangeWorkspaceMemberRoleRequest(
    @Schema(
        implementation = WorkspaceAssignableRoleCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull WorkspaceAssignableRoleCode role
) {
}
