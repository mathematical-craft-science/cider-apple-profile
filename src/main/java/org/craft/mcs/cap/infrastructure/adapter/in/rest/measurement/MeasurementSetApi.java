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
      description = "Records evidence against the expected current material state without changing that state. "
          + "The expected identifier protects the measurement from concurrent material lifecycle operations.",
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
      description = "The expected current material state is stale.",
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

  @Operation(summary = "List measurement sets for one business subject")
  @ApiResponse(
      responseCode = "200",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = MeasurementSetListResponse.class)
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
  @NonNull ResponseEntity<Void> retract(
      @Parameter(required = true, schema = @Schema(format = "uuid"))
      @NonNull UUID measurementSetId,
      @Parameter(
          description = "Optimistic-locking version observed by the caller.",
          required = true,
          schema = @Schema(minimum = "0")
      )
      long expectedVersion
  );
}
