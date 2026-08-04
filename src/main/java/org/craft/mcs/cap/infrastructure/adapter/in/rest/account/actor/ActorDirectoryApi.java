package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.actor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.actor.dto.ActorDisplayResponse;
import org.craft.mcs.cap.shared.kernel.identity.ActorType;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/** OpenAPI contract for resolving durable actor references used by audit and operation history. */
@Tag(
    name = "Actor Directory",
    description = "Provider-neutral display identities for human and machine authors."
)
public interface ActorDirectoryApi {

  @Operation(
      operationId = "getActorDisplay",
      summary = "Resolve an actor display identity",
      description = "Returns minimal display data for an actor associated with the active workspace. "
          + "Removed memberships and deleted users remain resolvable for historical attribution."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Actor display identity.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ActorDisplayResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "The selected workspace is not accessible to the authenticated account.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "An active workspace must be selected when the account has multiple memberships.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Actor has no retained association with the active workspace.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull ActorDisplayResponse> get(
      @Parameter(description = "Actor category.", required = true)
      @NonNull ActorType actorType,
      @Parameter(description = "Stable CAP-owned actor identifier.", required = true)
      @NonNull UUID actorId
  );
}
