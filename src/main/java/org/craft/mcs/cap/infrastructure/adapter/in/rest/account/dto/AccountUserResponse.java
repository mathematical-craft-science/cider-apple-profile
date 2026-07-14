package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** CAP-owned public profile fields of the current authenticated user. */
@Schema(description = "CAP-owned account profile. Authentication credentials remain provider-owned.")
public record AccountUserResponse(
    @Schema(
        description = "Stable CAP-owned identifier of the account user.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID userId,
    @Schema(
        description = "Email obtained from the external identity provider.",
        format = "email",
        example = "technologist@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String email,
    @Schema(
        description = "Human-readable name used to represent the user inside CAP.",
        example = "Alex Cidermaker",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String displayName,
    @Schema(
        description = "Optional absolute HTTP or HTTPS URL of the user's avatar.",
        format = "uri",
        example = "https://images.example.com/users/alex.png",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String avatarUrl,
    @Schema(
        description = "CAP account lifecycle status controlling product access.",
        implementation = AccountUserStatus.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AccountUserStatus status
) {
}
