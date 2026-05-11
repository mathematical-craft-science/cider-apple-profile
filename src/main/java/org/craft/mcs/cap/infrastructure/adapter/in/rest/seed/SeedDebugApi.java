package org.craft.mcs.cap.infrastructure.adapter.in.rest.seed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.seed.dto.SeedImportResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Debug seed", description = "Development-only seed import endpoints.")
public interface SeedDebugApi {

  @Operation(
      summary = "Import apple variety seed",
      description = "Imports apple variety seed data from the bundled JSON resource. Available in dev/local profiles only."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Seed imported.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = SeedImportResponse.class))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Seed resource not found.",
      content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = SeedImportResponse.class))
  )
  @NonNull ResponseEntity<@NonNull SeedImportResponse> importAppleVarieties();
}
