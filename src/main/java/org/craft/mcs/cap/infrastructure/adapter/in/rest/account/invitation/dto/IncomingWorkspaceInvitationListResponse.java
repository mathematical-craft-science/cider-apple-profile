package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;

/** Incoming workspace invitations for the current account, ordered from newest to oldest. */
@Schema(description = "Workspace invitations addressed to the authenticated CAP account.")
public record IncomingWorkspaceInvitationListResponse(
    @ArraySchema(
        schema = @Schema(implementation = IncomingWorkspaceInvitationResponse.class),
        arraySchema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    )
    @NonNull List<@NonNull IncomingWorkspaceInvitationResponse> invitations
) {
}
