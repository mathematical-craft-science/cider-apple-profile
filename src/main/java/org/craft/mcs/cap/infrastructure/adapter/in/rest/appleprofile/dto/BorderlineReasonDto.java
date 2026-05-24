package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Borderline classification reason.")
public record BorderlineReasonDto(
    @Schema(description = "Measured profile axis.", example = "SUGAR", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String measure,
    @Schema(description = "Condition describing the boundary.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String borderlineCondition
) {

  public BorderlineReasonDto {
    requireNonNull(measure, "measure cannot be null");
    requireNonNull(borderlineCondition, "borderlineCondition cannot be null");
  }
}
