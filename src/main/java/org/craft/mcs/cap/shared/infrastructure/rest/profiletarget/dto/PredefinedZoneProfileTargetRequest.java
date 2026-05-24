package org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Target that references one of the built-in sugar-acidity zones.")
public record PredefinedZoneProfileTargetRequest(
    @Schema(description = "Target shape.", example = "PREDEFINED_ZONE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String kind,
    @Schema(description = "Built-in sugar-acidity zone.", implementation = SugarAcidityZone.class, example = "PERFECT_APPLES", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityZone zone
) implements ProfileTargetRequest {

  public PredefinedZoneProfileTargetRequest {
    requireNonNull(kind, "target kind cannot be null");
    requireNonNull(zone, "sugar-acidity zone cannot be null");
  }
}
