package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.account.domain.model.AccountPermission;
import org.jspecify.annotations.NonNull;


/** Complete current-account context required by authenticated CAP clients. */
@Schema(description = "Current CAP account, active workspace, memberships, preferences, and permissions.")
public record AccountMeResponse(
    @Schema(
        description = "CAP-owned profile of the current authenticated user.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AccountUserResponse user,
    @Schema(
        description = "Workspace currently used to evaluate workspace-scoped permissions.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AccountWorkspaceResponse activeWorkspace,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Active workspace memberships accessible to the current user.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = AccountMembershipResponse.class)
    )
    @NonNull List<@NonNull AccountMembershipResponse> memberships,
    @Schema(
        description = "Personal locale and time-zone preferences of the current user.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AccountPreferencesResponse preferences,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Permissions derived from the user's role in the active workspace.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = AccountPermission.class)
    )
    @NonNull Set<@NonNull AccountPermission> permissions
) {

  public AccountMeResponse {
    memberships = List.copyOf(memberships);
    permissions = Set.copyOf(permissions);
  }
}
