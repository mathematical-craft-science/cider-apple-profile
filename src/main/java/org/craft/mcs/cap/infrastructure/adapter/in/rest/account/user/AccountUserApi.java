package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.AccountPreferencesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.AccountUserResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.UpdateAccountPreferencesRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.dto.UpdateAccountProfileRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** OpenAPI contract for the current user's CAP-owned profile and preferences. */
@Tag(name = "Account Profile", description = "Current user's CAP-owned profile and preferences.")
public interface AccountUserApi {

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
}
