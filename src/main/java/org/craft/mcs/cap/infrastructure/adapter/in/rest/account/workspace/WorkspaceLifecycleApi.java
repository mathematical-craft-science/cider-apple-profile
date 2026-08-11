package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.AccountMembershipResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.AccountWorkspaceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.CreateWorkspaceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.UpdateWorkspaceRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** OpenAPI contract for CAP workspace lifecycle. */
@Tag(name = "Workspace Administration", description = "CAP workspace creation, metadata, and archival.")
public interface WorkspaceLifecycleApi {

  @Operation(summary = "Create an additional workspace")
  @ApiResponse(
      responseCode = "201",
      description = "Workspace and creator OWNER membership created atomically.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountMembershipResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @NonNull ResponseEntity<@NonNull AccountMembershipResponse> createWorkspace(
      @Valid @NonNull CreateWorkspaceRequest request
  );

  @Operation(summary = "Update workspace metadata")
  @ApiResponse(
      responseCode = "200",
      description = "Updated active workspace.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountWorkspaceResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "Workspace update permission is missing.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Workspace was not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AccountWorkspaceResponse> updateWorkspace(
      @NonNull UUID workspaceId,
      @Valid @NonNull UpdateWorkspaceRequest request
  );

  @Operation(summary = "Archive a workspace")
  @ApiResponse(
      responseCode = "200",
      description = "Archived workspace retained for historical references.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountWorkspaceResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "Only an active workspace owner can archive the workspace.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AccountWorkspaceResponse> archiveWorkspace(
      @NonNull UUID workspaceId
  );
}
