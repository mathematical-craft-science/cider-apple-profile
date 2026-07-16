package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.UpdateWorkspaceSettingsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.WorkspaceSettingsResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** OpenAPI contract for CAP-owned workspace settings. */
@Tag(name = "Workspace Settings", description = "Versioned CAP workspace settings.")
public interface WorkspaceSettingsApi {

  @Operation(summary = "Get workspace settings")
  @ApiResponse(
      responseCode = "200",
      description = "Workspace settings with revision and audit timestamps.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = WorkspaceSettingsResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "Current actor cannot read settings for this workspace.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull WorkspaceSettingsResponse> workspaceSettings(
      @NonNull UUID workspaceId
  );

  @Operation(summary = "Update workspace settings")
  @ApiResponse(
      responseCode = "200",
      description = "Updated workspace settings with the next revision.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = WorkspaceSettingsResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "409",
      description = "Settings changed after expectedRevision was read.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull WorkspaceSettingsResponse> updateWorkspaceSettings(
      @NonNull UUID workspaceId,
      @NonNull UpdateWorkspaceSettingsRequest request
  );
}
