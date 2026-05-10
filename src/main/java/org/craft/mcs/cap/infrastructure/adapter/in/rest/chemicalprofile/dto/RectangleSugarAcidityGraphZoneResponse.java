package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.SugarAcidityZone;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.plot.SugarAcidityGraphZoneShape;
import org.jspecify.annotations.NonNull;


@Schema(description = "Rectangular sugar-acidity graph zone.")
public record RectangleSugarAcidityGraphZoneResponse(
    @Schema(description = "Zone code.", implementation = SugarAcidityZone.class, example = "SUGAR_RICH_APPLES") @NonNull SugarAcidityZone zone,
    @Schema(description = "Specific gravity range for the zone.") @NonNull SugarAcidityGraphRangeResponse sugarRange,
    @Schema(description = "Titratable acidity range for the zone.") @NonNull SugarAcidityGraphRangeResponse acidityRange
) implements SugarAcidityGraphZoneResponse {

  @Override
  @Schema(description = "Zone shape discriminator.", implementation = SugarAcidityGraphZoneShape.class, example = "RECTANGLE")
  public @NonNull SugarAcidityGraphZoneShape shape() {
    return SugarAcidityGraphZoneShape.RECTANGLE;
  }
}
