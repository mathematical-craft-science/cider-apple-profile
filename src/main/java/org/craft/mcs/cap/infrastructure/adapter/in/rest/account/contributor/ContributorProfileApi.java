package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.contributor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.contributor.dto.ConfigureContributorProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.contributor.dto.ContributorProfileResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/** OpenAPI contract for the current account's CAP-owned contributor profile. */
@Tag(name = "Contributor Profile", description = "Public contributor identity and contact publication consent.")
public interface ContributorProfileApi {

  @Operation(summary = "Get current contributor profile")
  @ApiResponse(
      responseCode = "200",
      description = "Current account's complete contributor-profile management view.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ContributorProfileResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "404",
      description = "Contributor profile has not been created.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull ContributorProfileResponse> contributorProfile();

  @Operation(summary = "Create or replace current contributor profile")
  @ApiResponse(
      responseCode = "200",
      description = "Contributor profile and contact-publication consent saved.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ContributorProfileResponse.class)
      )
  )
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "422",
      description = "Contributor profile violates a business rule.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull ContributorProfileResponse> configureContributorProfile(
      @NonNull ConfigureContributorProfileRequest request
  );

  @Operation(summary = "Delete current contributor profile")
  @ApiResponse(responseCode = "204", description = "Contributor profile deleted.", content = @Content)
  @ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content)
  @ApiResponse(
      responseCode = "404",
      description = "Contributor profile has not been created.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull Void> deleteContributorProfile();
}
