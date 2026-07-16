package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.WorkspaceAssignableRoleCode;
import org.jspecify.annotations.NonNull;


/**
 * Request to create a pre-membership workspace invitation.
 */
@Schema(description = "Workspace invitation recipient and future non-owner role.")
public record InviteWorkspaceMemberRequest(
    @Schema(example = "technologist@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email @NotBlank @NonNull String email,
    @Schema(
        implementation = WorkspaceAssignableRoleCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull WorkspaceAssignableRoleCode role
) {
}
