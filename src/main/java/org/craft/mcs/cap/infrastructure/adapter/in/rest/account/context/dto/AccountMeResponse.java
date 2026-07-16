package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.context.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission;
import org.craft.mcs.cap.core.account.user.domain.model.PersonalAccountCapability;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.AccountMembershipResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.AccountPreferencesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.AccountUserResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.AccountWorkspaceResponse;
import org.jspecify.annotations.NonNull;


/** Complete current-account context required by authenticated CAP clients. */
@Schema(
    description = "Current CAP account with personal capabilities and active-workspace permissions."
)
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
            description = "Capabilities over the current user's own CAP profile and preferences.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = PersonalAccountCapability.class)
    )
    @NonNull Set<@NonNull PersonalAccountCapability> personalCapabilities,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Permissions derived from membership role in the active workspace.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = WorkspacePermission.class)
    )
    @NonNull Set<@NonNull WorkspacePermission> workspacePermissions
) {

  public AccountMeResponse {
    memberships = List.copyOf(memberships);
    personalCapabilities = Set.copyOf(personalCapabilities);
    workspacePermissions = Set.copyOf(workspacePermissions);
  }
}
