package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


/** Collection response for membership details visible within one workspace. */
@Schema(description = "Members of a CAP workspace visible to the current actor.")
public record AccountWorkspaceMemberListResponse(
    @ArraySchema(
        arraySchema = @Schema(
            description = "Workspace members visible to the current authenticated actor.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = AccountWorkspaceMemberResponse.class)
    )
    @NonNull List<@NonNull AccountWorkspaceMemberResponse> members
) {

  public AccountWorkspaceMemberListResponse {
    members = List.copyOf(members);
  }
}
