package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
import org.jspecify.annotations.NonNull;

/** Transport representation of the current user's membership in one CAP workspace. */
@Schema(description = "Current user's CAP-owned workspace membership.")
public record AccountMembershipResponse(
    @Schema(
        description = "Stable identifier of the user's membership in this workspace.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID membershipId,
    @Schema(
        description = "CAP workspace to which the membership grants access.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AccountWorkspaceResponse workspace,
    @Schema(
        description = "CAP role assigned to the user within this workspace.",
        implementation = AccountRole.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AccountRole role,
    @Schema(
        description = "Lifecycle status controlling whether this membership grants permissions.",
        implementation = WorkspaceMembershipStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull WorkspaceMembershipStatus status
) {
}
