package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.CreateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.FeedstockBatchListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.FeedstockBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.PatchFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.UpdateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Feedstock batches",
    description = "Feedstock batch workspace endpoints: create, list, update and soft-delete source material batches."
)
public interface FeedstockBatchApi {

  @Operation(
      summary = "Create feedstock batch",
      description = """
          Creates a feedstock batch with optional vessel, season dates, optional source and
          available volume.
          """,
      requestBody = @RequestBody(
          description = "Feedstock batch creation payload.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CreateFeedstockBatchRequest.class)
          )
      )
  )
  @ApiResponse(
      responseCode = "201",
      description = "Feedstock batch created.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockBatchResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid feedstock batch request.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchResponse> create(
      @NonNull CreateFeedstockBatchRequest request
  );

  @Operation(
      summary = "List feedstock batches",
      description = "Lists feedstock batches by workspace, optionally filtered by an overlapping season date range."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock batch list.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockBatchListResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid season filter, for example only one season boundary was provided.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchListResponse> list(
      @Parameter(description = "Workspace identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID workspaceId,
      @Parameter(
          description = "Optional season filter start date. Must be provided together with seasonEndsOn.",
          schema = @Schema(format = "date")
      )
      @Nullable LocalDate seasonStartsOn,
      @Parameter(
          description = "Optional season filter end date. Must be provided together with seasonStartsOn.",
          schema = @Schema(format = "date")
      )
      @Nullable LocalDate seasonEndsOn
  );

  @Operation(
      summary = "Get feedstock batch detail",
      description = """
          Returns feedstock batch metadata, measurement history and current-profile read model for a
          batch detail page. The current profile is derived from the latest measurement only.
          """
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock batch detail.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockBatchDetailResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "Batch belongs to another workspace.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchDetailResponse> get(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @Parameter(
          description = "Workspace identifier used for ownership validation.",
          required = true,
          schema = @Schema(format = "uuid")
      )
      @NonNull UUID workspaceId
  );

  @Operation(
      summary = "Replace feedstock batch",
      description = """
          Replaces editable feedstock batch fields. The request must include the owning workspace id
          for ownership validation.
          """,
      requestBody = @RequestBody(
          description = "Full feedstock batch update payload.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = UpdateFeedstockBatchRequest.class)
          )
      )
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock batch updated.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockBatchResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid feedstock batch update request.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "403",
      description = "Batch belongs to another workspace.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock batch was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchResponse> update(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @NonNull UpdateFeedstockBatchRequest request
  );

  @Operation(
      summary = "Patch feedstock batch",
      description = "Partially updates editable feedstock batch fields. Blank optional text values clear that field."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock batch patched.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockBatchResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid feedstock batch patch request.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "403",
      description = "Batch belongs to another workspace.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock batch was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull FeedstockBatchResponse> patch(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @RequestBody(
          description = "Partial feedstock batch update payload.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = PatchFeedstockBatchRequest.class)
          )
      )
      @NonNull PatchFeedstockBatchRequest request
  );

  @Operation(
      summary = "Delete feedstock batch",
      description = """
          Soft-deletes a feedstock batch. The batch disappears from normal workspace queries while
          historical rows remain in persistence.
          """
  )
  @ApiResponse(responseCode = "204", description = "Feedstock batch deleted.")
  @ApiResponse(
      responseCode = "403",
      description = "Batch belongs to another workspace.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock batch was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<Void> delete(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @Parameter(
          description = "Workspace identifier used for ownership validation.",
          required = true,
          schema = @Schema(format = "uuid")
      )
      @NonNull UUID workspaceId
  );
}
