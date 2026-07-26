package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.PatchFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.UpdateFeedstockMeasurementRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(
    name = "Feedstock measurements",
    description = "Deprecated Feedstock compatibility facade over generic MeasurementSet use cases."
)
public interface FeedstockMeasurementApi {

  @Operation(
      summary = "Create feedstock measurement",
      deprecated = true,
      description = "Deprecated compatibility operation. Use POST /measurement-sets with subject type "
          + "FEEDSTOCK_BATCH and observedMaterialStateId. The facade resolves the current state and delegates to the "
          + "same generic use case.",
      requestBody = @RequestBody(
          description = "Feedstock measurement creation payload.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CreateFeedstockMeasurementRequest.class)
          )
      )
  )
  @ApiResponse(
      responseCode = "201",
      description = "Feedstock measurement created.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockMeasurementResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid measurement payload, for example a measurement date in the future.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @Deprecated
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementResponse> create(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @NonNull CreateFeedstockMeasurementRequest request
  );

  @Operation(
      summary = "List feedstock measurements by batch",
      deprecated = true,
      description = "Deprecated compatibility operation. Use "
          + "GET /measurement-sets?subjectType=FEEDSTOCK_BATCH&subjectId={batchId}."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock measurement list.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockMeasurementListResponse.class)
      )
  )
  @Deprecated
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementListResponse> listByBatchId(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId
  );

  @Operation(
      summary = "Replace feedstock measurement",
      deprecated = true,
      description = "Deprecated compatibility operation. Use PUT /measurement-sets/{measurementSetId}."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock measurement updated.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockMeasurementResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid measurement payload, for example a measurement date in the future.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch or measurement not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock measurement was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @Deprecated
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementResponse> update(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @Parameter(description = "Feedstock measurement identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID measurementId,
      @RequestBody(
          description = "Full feedstock measurement update payload.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = UpdateFeedstockMeasurementRequest.class)
          )
      )
      @NonNull UpdateFeedstockMeasurementRequest request
  );

  @Operation(
      summary = "Patch feedstock measurement",
      deprecated = true,
      description = "Deprecated compatibility operation. Use PATCH /measurement-sets/{measurementSetId}."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Feedstock measurement patched.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedstockMeasurementResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid measurement payload, for example a measurement date in the future.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch or measurement not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock measurement was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @Deprecated
  @NonNull ResponseEntity<@NonNull FeedstockMeasurementResponse> patch(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @Parameter(description = "Feedstock measurement identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID measurementId,
      @RequestBody(
          description = "Partial feedstock measurement update payload.",
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = PatchFeedstockMeasurementRequest.class)
          )
      )
      @NonNull PatchFeedstockMeasurementRequest request
  );

  @Operation(
      summary = "Delete feedstock measurement",
      deprecated = true,
      description = "Deprecated compatibility operation. Use DELETE /measurement-sets/{measurementSetId}. "
          + "Both operations retract evidence without physical deletion."
  )
  @ApiResponse(responseCode = "204", description = "Feedstock measurement deleted.")
  @ApiResponse(
      responseCode = "404",
      description = "Feedstock batch or measurement not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @ApiResponse(
      responseCode = "409",
      description = "Feedstock measurement was modified concurrently.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @Deprecated
  @NonNull ResponseEntity<Void> delete(
      @Parameter(description = "Feedstock batch identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID batchId,
      @Parameter(description = "Feedstock measurement identifier.", required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID measurementId
  );
}
