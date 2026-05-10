package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Evidence explaining why classification is close to a threshold boundary.")
public record BorderlineEvidenceResponse(
    @Schema(description = "Measured profile axis.", example = "ACIDITY") @NonNull String measure,
    @Schema(description = "Condition describing the boundary.", example = "near HIGH threshold") @NonNull String borderlineCondition
) {

  public BorderlineEvidenceResponse {
    requireNonNull(measure, "measure cannot be null");
    requireNonNull(borderlineCondition, "borderlineCondition cannot be null");
  }

}
