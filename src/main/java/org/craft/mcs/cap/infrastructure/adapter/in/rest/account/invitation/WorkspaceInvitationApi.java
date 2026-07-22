package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.AccountMembershipResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.InviteWorkspaceMemberRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.IncomingWorkspaceInvitationListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.IncomingWorkspaceInvitationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.WorkspaceInvitationListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.WorkspaceInvitationResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * OpenAPI contract for workspace invitation lifecycle before membership creation.
 */
@Tag(name = "Workspace Invitations", description = "Invite, accept, reject, revoke, expire, and resend lifecycle.")
public interface WorkspaceInvitationApi {

  @Operation(summary = "List current account invitations")
  @ApiResponse(
      responseCode = "200",
      description = "Incoming invitations ordered from newest to oldest.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = IncomingWorkspaceInvitationListResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @NonNull ResponseEntity<@NonNull IncomingWorkspaceInvitationListResponse> incomingInvitations();

  @Operation(summary = "Preview current account invitation")
  @ApiResponse(
      responseCode = "200",
      description = "Invitation preview with workspace, role, status, and inviter context.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = IncomingWorkspaceInvitationResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "Invitation belongs to another account.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Invitation, workspace, or inviter was not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull IncomingWorkspaceInvitationResponse> incomingInvitation(
      @NonNull UUID invitationId
  );

  @Operation(
      summary = "Invite a workspace member",
      description = "Creates a pending invitation and atomically schedules asynchronous email delivery."
  )
  @ApiResponse(
      responseCode = "201",
      description = "Pending invitation created and its email delivery scheduled.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = WorkspaceInvitationResponse.class)
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
      responseCode = "409",
      description = "A pending invitation or membership already exists.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> invite(
      @NonNull UUID workspaceId,
      @NonNull InviteWorkspaceMemberRequest request
  );

  @Operation(summary = "List workspace invitations")
  @ApiResponse(
      responseCode = "200",
      description = "Invitations with time-aware effective status.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = WorkspaceInvitationListResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "Invitation read permission is missing.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull WorkspaceInvitationListResponse> invitations(
      @NonNull UUID workspaceId
  );

  @Operation(summary = "Accept current account invitation")
  @ApiResponse(
      responseCode = "200",
      description = "Invitation accepted and active membership created atomically.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountMembershipResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "Invitation belongs to another account.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Invitation was not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "409",
      description = "Workspace is archived or membership already exists.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Invitation cannot be accepted in its current state.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull AccountMembershipResponse> accept(@NonNull UUID invitationId);

  @Operation(summary = "Reject current account invitation")
  @ApiResponse(
      responseCode = "200",
      description = "Invitation rejected by its intended recipient.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = WorkspaceInvitationResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "Invitation belongs to another account.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Invitation was not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Invitation cannot be rejected in its current state.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> reject(@NonNull UUID invitationId);

  @Operation(summary = "Revoke a pending workspace invitation")
  @ApiResponse(
      responseCode = "200",
      description = "Pending invitation revoked.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = WorkspaceInvitationResponse.class)
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
      description = "Invitation was not found in this workspace.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Invitation cannot be revoked in its current state.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> revoke(
      @NonNull UUID workspaceId,
      @NonNull UUID invitationId
  );

  @Operation(
      summary = "Resend a pending or expired workspace invitation",
      description = "Renews the invitation and atomically schedules another asynchronous email delivery."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Invitation validity period renewed and another email delivery scheduled.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = WorkspaceInvitationResponse.class)
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
      description = "Invitation was not found in this workspace.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Invitation cannot be resent in its current state.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> resend(
      @NonNull UUID workspaceId,
      @NonNull UUID invitationId
  );
}
