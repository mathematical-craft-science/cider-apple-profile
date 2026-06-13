package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain.SugarAcidityGraphZoneShape;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Elliptical sugar-acidity graph zone.")
public record EllipseSugarAcidityGraphZoneResponse(
    @Schema(description = "Zone code.", implementation = SugarAcidityZone.class, example = "PERFECT_APPLES", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull SugarAcidityZone zone,
    @Schema(description = "Specific gravity coordinate of ellipse center.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse sugarCenter,
    @Schema(description = "Titratable acidity coordinate of ellipse center.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse acidityCenter,
    @Schema(description = "Specific gravity ellipse radius.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse sugarRadius,
    @Schema(description = "Titratable acidity ellipse radius.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse acidityRadius
) implements SugarAcidityGraphZoneResponse {

  @Override
  @Schema(description = "Zone shape discriminator.", implementation = SugarAcidityGraphZoneShape.class, example = "ELLIPSE", requiredMode = Schema.RequiredMode.REQUIRED)
  public @NonNull SugarAcidityGraphZoneShape shape() {
    return SugarAcidityGraphZoneShape.ELLIPSE;
  }
}
