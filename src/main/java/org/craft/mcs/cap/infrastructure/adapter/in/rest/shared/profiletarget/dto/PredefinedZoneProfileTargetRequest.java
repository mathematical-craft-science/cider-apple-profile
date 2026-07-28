package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Target that references one of the built-in Jolicoeur sugar-acidity zones.")
public record PredefinedZoneProfileTargetRequest(
    @Schema(
        description = "Target kind.",
        type = "string",
        allowableValues = "PREDEFINED_ZONE",
        example = "PREDEFINED_ZONE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String kind,
    @Schema(
        description = "Built-in Jolicoeur sugar-acidity zone.",
        implementation = JolicoeurSugarAcidityZone.class,
        example = "PERFECT_APPLES",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull JolicoeurSugarAcidityZone zone
) implements ProfileTargetRequest {

  public PredefinedZoneProfileTargetRequest {
    requireNonNull(kind, "target kind cannot be null");
    requireNonNull(zone, "sugar-acidity zone cannot be null");
  }
}
