package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.jspecify.annotations.NonNull;


/** Request transferring ownership to another active workspace membership. */
@Schema(description = "Explicit ownership transfer target.")
public record TransferWorkspaceOwnershipRequest(
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @NonNull UUID targetMembershipId
) {
}
