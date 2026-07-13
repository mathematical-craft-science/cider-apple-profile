package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembershipStatus;
import org.jspecify.annotations.NonNull;


/** Transport projection joining a CAP user with their workspace membership state. */
@Schema(description = "CAP user and membership state within a workspace.")
public record AccountWorkspaceMemberResponse(
    @Schema(
        description = "Stable identifier of this user's membership in the requested workspace.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID membershipId,
    @Schema(
        description = "CAP-owned profile of the workspace member.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AccountUserResponse user,
    @Schema(
        description = "CAP role assigned to this member within the workspace.",
        implementation = AccountRole.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AccountRole role,
    @Schema(
        description = "Lifecycle status controlling whether the membership grants permissions.",
        implementation = WorkspaceMembershipStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull WorkspaceMembershipStatus status
) {
}
