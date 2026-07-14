package org.craft.mcs.cap.infrastructure.adapter.in.rest.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountMeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountPreferencesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountUserResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountWorkspaceMemberListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountWorkspaceListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.UpdateAccountPreferencesRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.UpdateAccountProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.UpdateWorkspaceSettingsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.WorkspaceSettingsResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.craft.mcs.cap.infrastructure.adapter.in.http.ActiveWorkspaceHttpHeaders.ACTIVE_WORKSPACE_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/** OpenAPI contract for CAP-owned account, preference, and workspace queries and updates. */
@Tag(name = "Account", description = "CAP-owned user, workspace, membership, and preference endpoints.")
public interface AccountApi {

  @Operation(
      summary = "Get current CAP account",
      description = "Returns the account context for the explicitly selected active workspace. "
          + "The header may be omitted only when the account has exactly one active workspace.",
      parameters = @Parameter(
          name = ACTIVE_WORKSPACE_ID,
          description = "Active CAP workspace identifier for this stateless request.",
          in = ParameterIn.HEADER,
          schema = @Schema(type = "string", format = "uuid")
      )
  )
  @ApiResponse(
      responseCode = "200",
      description = "Current account context.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountMeResponse.class))
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "400",
      description = "The active workspace header is not a valid UUID.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "403",
      description = "The selected workspace is not accessible to the current account.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "An explicit workspace selection is required for a multi-workspace account.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AccountMeResponse> me();

  @Operation(summary = "Get current CAP profile")
  @ApiResponse(
      responseCode = "200",
      description = "Current CAP-owned user profile.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountUserResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @NonNull ResponseEntity<@NonNull AccountUserResponse> profile();

  @Operation(summary = "Update current CAP profile")
  @RequestBody(
      required = true,
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = UpdateAccountProfileRequest.class)
      )
  )
  @ApiResponse(
      responseCode = "200",
      description = "Updated CAP-owned user profile.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountUserResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @NonNull ResponseEntity<@NonNull AccountUserResponse> updateProfile(
      @NonNull UpdateAccountProfileRequest request
  );

  @Operation(summary = "Get current user preferences")
  @ApiResponse(
      responseCode = "200",
      description = "Current CAP-owned user preferences.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountPreferencesResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @NonNull ResponseEntity<@NonNull AccountPreferencesResponse> preferences();

  @Operation(summary = "Update current user preferences")
  @RequestBody(
      required = true,
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = UpdateAccountPreferencesRequest.class)
      )
  )
  @ApiResponse(
      responseCode = "200",
      description = "Updated CAP-owned user preferences.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountPreferencesResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid locale or time-zone value.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @NonNull ResponseEntity<@NonNull AccountPreferencesResponse> updatePreferences(
      @NonNull UpdateAccountPreferencesRequest request
  );

  @Operation(summary = "List current account workspaces")
  @ApiResponse(
      responseCode = "200",
      description = "Active workspace memberships accessible to the current account.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountWorkspaceListResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @NonNull ResponseEntity<@NonNull AccountWorkspaceListResponse> workspaces();

  @Operation(
      summary = "Get workspace settings",
      parameters = @Parameter(
          name = ACTIVE_WORKSPACE_ID,
          description = "Active CAP workspace identifier; it must match workspaceId.",
          in = ParameterIn.HEADER,
          schema = @Schema(type = "string", format = "uuid")
      )
  )
  @ApiResponse(
      responseCode = "200",
      description = "Workspace settings with revision and audit timestamps.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = WorkspaceSettingsResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "400",
      description = "The active workspace header is not a valid UUID.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "403",
      description = "Current actor cannot read settings for this workspace.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Workspace settings were not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "An explicit workspace selection is required for a multi-workspace account.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull WorkspaceSettingsResponse> workspaceSettings(
      @Parameter(description = "CAP workspace identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID workspaceId
  );

  @Operation(
      summary = "Update workspace settings",
      parameters = @Parameter(
          name = ACTIVE_WORKSPACE_ID,
          description = "Active CAP workspace identifier; it must match workspaceId.",
          in = ParameterIn.HEADER,
          schema = @Schema(type = "string", format = "uuid")
      )
  )
  @RequestBody(
      required = true,
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = UpdateWorkspaceSettingsRequest.class)
      )
  )
  @ApiResponse(
      responseCode = "200",
      description = "Updated workspace settings with the next revision.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = WorkspaceSettingsResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid active-workspace header or settings update.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "Current actor cannot update settings for this workspace.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Workspace settings were not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Settings changed after expectedRevision was read.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "Settings violate a workspace business rule.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull WorkspaceSettingsResponse> updateWorkspaceSettings(
      @Parameter(description = "CAP workspace identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID workspaceId,
      @NonNull UpdateWorkspaceSettingsRequest request
  );

  @Operation(
      summary = "List workspace members",
      parameters = @Parameter(
          name = ACTIVE_WORKSPACE_ID,
          description = "Active CAP workspace identifier; it must match workspaceId.",
          in = ParameterIn.HEADER,
          schema = @Schema(type = "string", format = "uuid")
      )
  )
  @ApiResponse(
      responseCode = "200",
      description = "Workspace members visible to the current actor.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountWorkspaceMemberListResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "400",
      description = "The active workspace header is not a valid UUID.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "403",
      description = "Current actor cannot access the workspace member list.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "An explicit workspace selection is required for a multi-workspace account.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AccountWorkspaceMemberListResponse> workspaceMembers(
      @Parameter(description = "CAP workspace identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID workspaceId
  );
}
