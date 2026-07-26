package org.craft.mcs.cap.infrastructure.adapter.in.rest.materialstate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.materialstate.dto.CurrentMaterialStateResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Material states", description = "Current immutable states of measurable material subjects.")
public interface MaterialStateApi {

  @Operation(
      summary = "Get the current material state",
      description = "Returns the subject's current state identifier together with applicable characteristic "
          + "values and their immutable evidence. The identifier can bind a new measurement to the sampled state."
  )
  @ApiResponse(
      responseCode = "200",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = CurrentMaterialStateResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "The material subject has no initialized state.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull CurrentMaterialStateResponse> getCurrent(
      @Parameter(description = "Measurable material subject type.", required = true)
      @NonNull MeasurementSubjectType subjectType,
      @Parameter(
          description = "Business subject identifier.",
          required = true,
          schema = @Schema(format = "uuid")
      )
      @NonNull UUID subjectId
  );
}
