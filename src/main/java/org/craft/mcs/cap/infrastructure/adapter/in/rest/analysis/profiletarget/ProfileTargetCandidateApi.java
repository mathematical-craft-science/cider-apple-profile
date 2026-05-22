package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.AppleVarietyProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.AppleVarietyProfileTargetCandidatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.FeedstockBatchProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.dto.FeedstockBatchProfileTargetCandidatesResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Profile target analysis",
    description = "Target-aware candidate search for apple varieties and feedstock batches."
)
public interface ProfileTargetCandidateApi {

  @Operation(
      summary = "Search apple variety profile-target candidates",
      description = "Returns apple varieties scored against a selected profile target, including target distance and contribution role."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Apple variety candidates.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AppleVarietyProfileTargetCandidatesResponse.class))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Malformed request or unsupported target definition.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull AppleVarietyProfileTargetCandidatesResponse> appleVarietyCandidates(
      @RequestBody(
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AppleVarietyProfileTargetCandidateSearchRequest.class))
      )
      @NonNull AppleVarietyProfileTargetCandidateSearchRequest request
  );

  @Operation(
      summary = "Search feedstock batch profile-target candidates",
      description = "Returns current feedstock batch profiles scored against a selected target, including target distance and contribution role."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock batch candidates.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchProfileTargetCandidatesResponse.class))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Malformed request or unsupported target definition.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchProfileTargetCandidatesResponse> feedstockBatchCandidates(
      @RequestBody(
          required = true,
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = FeedstockBatchProfileTargetCandidateSearchRequest.class))
      )
      @NonNull FeedstockBatchProfileTargetCandidateSearchRequest request
  );
}
