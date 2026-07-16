package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.context;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.context.dto.AccountMeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.AccountWorkspaceListResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.craft.mcs.cap.infrastructure.adapter.in.http.ActiveWorkspaceHttpHeaders.ACTIVE_WORKSPACE_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** OpenAPI contract for the authenticated account and active-workspace context. */
@Tag(name = "Account Context", description = "Current CAP account and accessible workspace context.")
public interface AccountContextApi {

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
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = AccountMeResponse.class)
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
      description = "The selected workspace is not accessible to the current account.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "422",
      description = "An explicit workspace selection is required for a multi-workspace account.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AccountMeResponse> me();

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
}
