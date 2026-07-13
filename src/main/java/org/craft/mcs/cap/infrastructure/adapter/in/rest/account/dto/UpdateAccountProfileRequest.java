package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Request for changing only profile fields owned by CAP. */
@Schema(description = "CAP-owned account profile fields. Email and security settings are provider-owned.")
public record UpdateAccountProfileRequest(
    @Schema(
        description = "Human-readable name used to represent the current user inside CAP.",
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
    @Nullable String avatarUrl
) {

  public UpdateAccountProfileRequest {
    requireNonNull(displayName, "account display name cannot be null");
  }
}
