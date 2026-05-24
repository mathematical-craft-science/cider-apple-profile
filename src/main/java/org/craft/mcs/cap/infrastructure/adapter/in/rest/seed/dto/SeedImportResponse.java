package org.craft.mcs.cap.infrastructure.adapter.in.rest.seed.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Development seed import response.")
public record SeedImportResponse(
    @Schema(description = "Seed import result message.", example = "Apple variety seed imported successfully", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String message
) {

  public SeedImportResponse {
    requireNonNull(message, "seed import response message cannot be null");
  }
}
