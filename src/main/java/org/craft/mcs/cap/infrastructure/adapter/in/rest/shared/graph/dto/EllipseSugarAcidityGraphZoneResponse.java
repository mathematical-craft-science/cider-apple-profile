package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Elliptical sugar-acidity graph zone expressed in SG and TA coordinates.")
public record EllipseSugarAcidityGraphZoneResponse(
    @Schema(
        description = "Jolicoeur zone code.",
        implementation = JolicoeurSugarAcidityZone.class,
        example = "PERFECT_APPLES",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull JolicoeurSugarAcidityZone zone,
    @Schema(
        description = "Specific Gravity (SG) coordinate of the ellipse center.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse specificGravityCenter,
    @Schema(
        description = "Titratable Acidity (TA) coordinate of the ellipse center.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse titratableAcidityCenter,
    @Schema(
        description = "Specific Gravity (SG) ellipse radius.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse specificGravityRadius,
    @Schema(
        description = "Titratable Acidity (TA) ellipse radius.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse titratableAcidityRadius
) implements SugarAcidityGraphZoneResponse {

  @Override
  @Schema(
      description = "Zone shape discriminator.",
      type = "string",
      allowableValues = "ELLIPSE",
      example = "ELLIPSE",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  public @NonNull SugarAcidityGeometryShape shape() {
    return SugarAcidityGeometryShape.ELLIPSE;
  }
}
