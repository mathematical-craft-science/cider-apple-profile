package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Transport representation of one workspace invitation lifecycle.
 */
@Schema(description = "Workspace invitation independent from registered membership.")
public record WorkspaceInvitationResponse(
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID invitationId,
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID workspaceId,
    @Schema(format = "email", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String invitedEmail,
    @Schema(implementation = AccountRole.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull AccountRole role,
    @Schema(implementation = WorkspaceInvitationStatus.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull WorkspaceInvitationStatus status,
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID invitedByUserId,
    @Schema(format = "uuid", nullable = true)
    @Nullable UUID acceptedByUserId,
    @Schema(format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull Instant expiresAt,
    @Schema(minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    int resendCount
) {
}
