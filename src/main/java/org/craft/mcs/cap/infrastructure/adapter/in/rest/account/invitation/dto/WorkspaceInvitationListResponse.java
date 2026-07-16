package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


/**
 * Workspace invitation collection ordered from newest to oldest.
 */
@Schema(description = "Workspace invitations visible to a workspace administrator.")
public record WorkspaceInvitationListResponse(
    @ArraySchema(
        schema = @Schema(implementation = WorkspaceInvitationResponse.class),
        arraySchema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    )
    @NonNull List<@NonNull WorkspaceInvitationResponse> invitations
) {
}
