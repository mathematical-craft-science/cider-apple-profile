package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.CreateMeasurementSetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.PatchMeasurementSetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.ReplaceMeasurementSetRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Measurement sets", description = "Generic immutable measurement evidence and corrections.")
public interface MeasurementSetApi {

  @Operation(
      summary = "Create a logical measurement set",
      description = "Records evidence against the immutable material state that was sampled without changing "
          + "the subject's current state. The observed state may be current or historical.",
      requestBody = @RequestBody(
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CreateMeasurementSetRequest.class)
          )
      )
  )
  @ApiResponse(
      responseCode = "201",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = MeasurementSetResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "409",
      description = "The caller-assigned measurement-set identifier already exists.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "The measured subject or observed material state was not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull MeasurementSetResponse> create(
      @NonNull CreateMeasurementSetRequest request
  );

  @Operation(summary = "Get the current revision of a measurement set")
  @ApiResponse(
      responseCode = "200",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = MeasurementSetResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull MeasurementSetResponse> get(
      @Parameter(
          description = "Logical measurement-set identifier.",
          required = true,
          schema = @Schema(format = "uuid")
      )
      @NonNull UUID measurementSetId
  );

  @Operation(
      summary = "List measurement sets for one business subject",
      description = """
          Returns current revisions of all logical sets, including RETRACTED evidence, ordered by
          logical-set update time descending and then measurementSetId descending.
          """
  )
  @ApiResponse(
      responseCode = "200",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = MeasurementSetListResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "The authenticated actor cannot read the measured subject.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "The measured subject was not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull MeasurementSetListResponse> list(
      @Parameter(description = "Measured aggregate type.", required = true)
      @NonNull MeasurementSubjectType subjectType,
      @Parameter(
          description = "Measured aggregate identifier.",
          required = true,
          schema = @Schema(format = "uuid")
      )
      @NonNull UUID subjectId
  );

  @Operation(
      summary = "Replace a measurement set",
      description = "Appends a full immutable correction revision; previous evidence is preserved.",
      requestBody = @RequestBody(
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ReplaceMeasurementSetRequest.class)
          )
      )
  )
  @ApiResponse(
      responseCode = "200",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = MeasurementSetResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "409",
      description = "The expected measurement-set version is stale.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "The measurement set was not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull MeasurementSetResponse> replace(
      @Parameter(required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID measurementSetId,
      @NonNull ReplaceMeasurementSetRequest request
  );

  @Operation(
      summary = "Patch a measurement set",
      description = "Appends a correction revision and copies unchanged evidence into that revision.",
      requestBody = @RequestBody(
          required = true,
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = PatchMeasurementSetRequest.class)
          )
      )
  )
  @ApiResponse(
      responseCode = "200",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = MeasurementSetResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "409",
      description = "The expected measurement-set version is stale.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "The measurement set was not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull MeasurementSetResponse> patch(
      @Parameter(required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID measurementSetId,
      @NonNull PatchMeasurementSetRequest request
  );

  @Operation(
      summary = "Retract a measurement set",
      description = "Marks the logical set as retracted without physically deleting revisions or records."
  )
  @ApiResponse(responseCode = "204", description = "Measurement set retracted.")
  @ApiResponse(
      responseCode = "404",
      description = "The measurement set was not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "409",
      description = "The expected version is stale or the measurement set is already retracted.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<Void> retract(
      @Parameter(required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID measurementSetId,
      @Parameter(
          description = "Optimistic-locking version observed by the caller.",
          required = true,
          schema = @Schema(type = "integer", format = "int64", minimum = "0")
      )
      long expectedVersion
  );
}
