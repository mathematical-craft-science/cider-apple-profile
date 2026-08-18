package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementevidence;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementevidence.dto.CurrentMeasurementEvidenceResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/** Defines the canonical HTTP contract for querying current Measurement evidence. */
@Tag(
    name = "Measurement evidence",
    description = "State-aware current measurement evidence for measurable subjects."
)
public interface MeasurementEvidenceApi {

  @Operation(
      summary = "Get current measurement evidence",
      description = "Returns measurement values currently applicable to the subject. Evidence is selected "
          + "independently per characteristic from the authoritative material-state lineage; the response does "
          + "not represent or own the material aggregate state."
  )
  @ApiResponse(
      responseCode = "200",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = CurrentMeasurementEvidenceResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "The measurable subject or its current material state was not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull CurrentMeasurementEvidenceResponse> getCurrentEvidence(
      @Parameter(description = "Measurable subject type.", required = true)
      @NonNull MeasurementSubjectType subjectType,
      @Parameter(
          description = "Business subject identifier.",
          required = true,
          schema = @Schema(format = "uuid")
      )
      @NonNull UUID subjectId
  );
}
