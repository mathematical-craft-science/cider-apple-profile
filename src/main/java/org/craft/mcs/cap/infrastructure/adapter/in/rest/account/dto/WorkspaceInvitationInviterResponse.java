package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Privacy-limited public identity of the account that sent an invitation. */
@Schema(description = "CAP profile summary of the account that sent the workspace invitation.")
public record WorkspaceInvitationInviterResponse(
    @Schema(
        description = "CAP account identifier of the invitation sender.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID userId,
    @Schema(
        description = "CAP-owned display name of the invitation sender.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String displayName,
    @Schema(
        description = "Optional CAP-owned avatar URL of the invitation sender.",
        format = "uri",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String avatarUrl
) {
}
