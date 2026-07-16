package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.audit.dto.WorkspaceAdministrativeAuditListResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** OpenAPI contract for immutable workspace administration history. */
@Tag(name = "Workspace Audit", description = "Immutable workspace administration history.")
public interface WorkspaceAuditApi {

  @Operation(summary = "List workspace administration audit")
  @ApiResponse(
      responseCode = "200",
      description = "Newest-first immutable workspace administration facts.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = WorkspaceAdministrativeAuditListResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "Audit read permission is missing.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull WorkspaceAdministrativeAuditListResponse> workspaceAudit(
      @NonNull UUID workspaceId
  );
}
