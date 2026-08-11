package org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.CreateVesselRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.UpdateVesselRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.VesselListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.VesselResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.FORMAT;
import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.PATTERN;


/** HTTP contract for the workspace Vessel catalog. */
@Tag(
    name = "Vessels",
    description = "Workspace-owned vessels and their authoritative catalog characteristics."
)
public interface VesselApi {

  @Operation(
      summary = "Register vessel",
      description = "Creates an operational vessel with a workspace-unique name and nominal capacity."
  )
  @ApiResponse(
      responseCode = "201",
      description = "Vessel registered.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = VesselResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid vessel request.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "Workspace write access is forbidden.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "409",
      description = "Vessel name already exists in the workspace.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Vessel characteristics violate a domain rule.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull VesselResponse> create(@Valid @NonNull CreateVesselRequest request);

  @Operation(
      summary = "List workspace vessels",
      description = "Returns the vessel catalog with authoritative current material usage, ordered by name."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Workspace vessel catalog.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = VesselListResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "Workspace read access is forbidden.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull VesselListResponse> list(
      @Parameter(
          description = "Workspace identifier.",
          required = true,
          example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
          schema = @Schema(format = "uuid")
      )
      @NonNull UUID workspaceId
  );

  @Operation(
      summary = "List available workspace vessels",
      description = "Returns operational vessels without active Feedstock or Production material. "
          + "The result is advisory; material assignment is validated again atomically when a batch is created."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Available workspace vessels ordered by name.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = VesselListResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Required volume filter is not greater than zero.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "Workspace read access is forbidden.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull VesselListResponse> listAvailable(
      @Parameter(
          description = "Workspace identifier.",
          required = true,
          example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
          schema = @Schema(format = "uuid")
      )
      @NonNull UUID workspaceId,
      @Parameter(
          description = "Optional case-insensitive substring of the vessel name.",
          example = "tank f"
      )
      @Nullable String nameContains,
      @Parameter(
          description = "Optional required material volume in liters. Returns vessels whose remaining capacity "
              + "is equal to or greater than this value.",
          example = "250.000",
          schema = @Schema(type = "number", minimum = "0", exclusiveMinimum = true)
      )
      @DecimalMin(value = "0", inclusive = false)
      @Nullable BigDecimal requiredVolumeLiters
  );

  @Operation(
      summary = "Get vessel",
      description = "Returns one vessel with its single current material assignment and remaining capacity."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Vessel found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = VesselResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "Vessel belongs to another workspace or workspace read access is forbidden.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Vessel not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @NonNull ResponseEntity<@NonNull VesselResponse> get(
      @Parameter(
          description = "Vessel UUIDv7 identifier.",
          required = true,
          schema = @Schema(format = FORMAT, pattern = PATTERN)
      )
      @NonNull UUID vesselId,
      @Parameter(
          description = "Owning workspace identifier.",
          required = true,
          schema = @Schema(format = "uuid")
      )
      @NonNull UUID workspaceId
  );

  @Operation(
      summary = "Update vessel",
      description = "Replaces editable catalog characteristics and operational availability using a revision token."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Vessel updated.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = VesselResponse.class)
      )
  )
  @ApiResponse(
      responseCode = "400",
      description = "Invalid vessel request.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "403",
      description = "Vessel belongs to another workspace or workspace write access is forbidden.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "404",
      description = "Vessel not found.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "409",
      description = "Stale revision, duplicate name, non-operational status for an occupied vessel, "
          + "or insufficient capacity.",
      content = @Content(
          mediaType = APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  @ApiResponse(
      responseCode = "422",
      description = "Vessel characteristics violate a domain rule.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
  )
  @NonNull ResponseEntity<@NonNull VesselResponse> update(
      @Parameter(
          description = "Vessel UUIDv7 identifier.",
          required = true,
          schema = @Schema(format = FORMAT, pattern = PATTERN)
      )
      @NonNull UUID vesselId,
      @Valid @NonNull UpdateVesselRequest request
  );
}
