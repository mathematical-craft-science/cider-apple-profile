package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZone;
import org.craft.mcs.cap.shared.domain.sugaracidity.plot.SugarAcidityGraphZoneShape;
import org.jspecify.annotations.NonNull;


@Schema(description = "Elliptical sugar-acidity graph zone.")
public record EllipseSugarAcidityGraphZoneResponse(
    @Schema(description = "Zone code.", implementation = SugarAcidityZone.class, example = "PERFECT_APPLES") @NonNull SugarAcidityZone zone,
    @Schema(description = "Specific gravity coordinate of ellipse center.", example = "1.066") @NonNull BigDecimal sugarCenter,
    @Schema(description = "Titratable acidity coordinate of ellipse center.", example = "6.0") @NonNull BigDecimal acidityCenter,
    @Schema(description = "Specific gravity ellipse radius.", example = "0.007") @NonNull BigDecimal sugarRadius,
    @Schema(description = "Titratable acidity ellipse radius.", example = "1.5") @NonNull BigDecimal acidityRadius
) implements SugarAcidityGraphZoneResponse {

  @Override
  @Schema(description = "Zone shape discriminator.", implementation = SugarAcidityGraphZoneShape.class, example = "ELLIPSE")
  public @NonNull SugarAcidityGraphZoneShape shape() {
    return SugarAcidityGraphZoneShape.ELLIPSE;
  }
}
