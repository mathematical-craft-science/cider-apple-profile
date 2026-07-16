package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.AccountMembershipResponse;
import org.jspecify.annotations.NonNull;


/** Collection response for workspace memberships accessible to the current user. */
@Schema(description = "Workspaces accessible to the current CAP account.")
public record AccountWorkspaceListResponse(
    @ArraySchema(
        arraySchema = @Schema(
            description = "Active workspace memberships accessible to the current user.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = AccountMembershipResponse.class)
    )
    @NonNull List<@NonNull AccountMembershipResponse> memberships
) {

  public AccountWorkspaceListResponse {
    memberships = List.copyOf(memberships);
  }
}
