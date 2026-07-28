package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.jspecify.annotations.NonNull;


@Schema(description = "Rectangular sugar-acidity graph zone expressed in SG and TA coordinates.")
public record RectangleSugarAcidityGraphZoneResponse(
    @Schema(
        description = "Jolicoeur zone code.",
        implementation = JolicoeurSugarAcidityZone.class,
        example = "HIGH_SUGAR_APPLES",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull JolicoeurSugarAcidityZone zone,
    @Schema(
        description = "Specific Gravity (SG) range for the zone.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull SugarAcidityGraphRangeResponse specificGravityRange,
    @Schema(
        description = "Titratable Acidity (TA) range for the zone.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull SugarAcidityGraphRangeResponse titratableAcidityRange
) implements SugarAcidityGraphZoneResponse {

  @Override
  @Schema(
      description = "Zone shape discriminator.",
      type = "string",
      allowableValues = "RECTANGLE",
      example = "RECTANGLE",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  public @NonNull SugarAcidityGeometryShape shape() {
    return SugarAcidityGeometryShape.RECTANGLE;
  }
}
