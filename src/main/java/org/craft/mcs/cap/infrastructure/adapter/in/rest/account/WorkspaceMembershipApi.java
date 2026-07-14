package org.craft.mcs.cap.infrastructure.adapter.in.rest.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountWorkspaceMemberResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.ChangeWorkspaceMemberRoleRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.TransferWorkspaceOwnershipRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * OpenAPI contract for workspace member access and ownership management.
 */
@Tag(name = "Workspace Membership", description = "Member roles, access state, removal, leave, and ownership.")
public interface WorkspaceMembershipApi {

  @Operation(summary = "Change a workspace member role")
  @ApiResponse(
      responseCode = "200",
      description = "Updated membership.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountWorkspaceMemberResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "Member-management permission is missing.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Membership was not found in this workspace.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "The last active owner cannot be downgraded.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AccountWorkspaceMemberResponse> changeRole(
      @NonNull UUID workspaceId,
      @NonNull UUID membershipId,
      @NonNull ChangeWorkspaceMemberRoleRequest request
  );

  @Operation(summary = "Suspend a workspace membership")
  @ApiResponse(
      responseCode = "200",
      description = "Membership suspended.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountWorkspaceMemberResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "Member-management permission is missing.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Membership was not found in this workspace.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Membership or ownership invariant prevents suspension.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull AccountWorkspaceMemberResponse> suspend(
      @NonNull UUID workspaceId,
      @NonNull UUID membershipId
  );

  @Operation(summary = "Reactivate a workspace membership")
  @ApiResponse(
      responseCode = "200",
      description = "Membership reactivated.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountWorkspaceMemberResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "Member-management permission is missing.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Membership was not found in this workspace.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Only a suspended membership can be reactivated.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull AccountWorkspaceMemberResponse> reactivate(
      @NonNull UUID workspaceId,
      @NonNull UUID membershipId
  );

  @Operation(summary = "Remove a workspace membership")
  @ApiResponse(responseCode = "204", description = "Membership removed.", content = @Content)
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "Member-management permission is missing.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Membership was not found in this workspace.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "The last active owner cannot be removed.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull Void> remove(
      @NonNull UUID workspaceId,
      @NonNull UUID membershipId
  );

  @Operation(summary = "Leave the active workspace")
  @ApiResponse(responseCode = "204", description = "Current member left the workspace.", content = @Content)
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "The workspace is not the active workspace.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "The last active owner must transfer ownership first.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull Void> leave(@NonNull UUID workspaceId);

  @Operation(summary = "Transfer workspace ownership")
  @ApiResponse(
      responseCode = "200",
      description = "Target membership promoted to OWNER and previous owner demoted to ADMIN.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountWorkspaceMemberResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "Only an active owner can transfer ownership.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Target membership was not found in this workspace.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Ownership transfer target is not eligible.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull AccountWorkspaceMemberResponse> transferOwnership(
      @NonNull UUID workspaceId,
      @NonNull TransferWorkspaceOwnershipRequest request
  );
}
