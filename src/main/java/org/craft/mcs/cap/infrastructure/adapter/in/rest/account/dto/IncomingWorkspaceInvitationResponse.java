package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.jspecify.annotations.NonNull;

/** Recipient-facing workspace invitation preview used before accept or reject. */
@Schema(description = "Incoming workspace invitation preview visible only to its intended recipient.")
public record IncomingWorkspaceInvitationResponse(
    @Schema(
        description = "Workspace invitation identifier.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID invitationId,
    @Schema(
        description = "Workspace the current account is invited to join.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AccountWorkspaceResponse workspace,
    @Schema(
        description = "Workspace role offered by the invitation.",
        implementation = AccountRole.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AccountRole role,
    @Schema(
        description = "Time-aware current invitation status.",
        implementation = WorkspaceInvitationStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull WorkspaceInvitationStatus status,
    @Schema(
        description = "CAP profile summary of the invitation sender.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull WorkspaceInvitationInviterResponse invitedBy,
    @Schema(
        description = "Expiration timestamp after which a pending invitation becomes expired.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant expiresAt
) {
}
