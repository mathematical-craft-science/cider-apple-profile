package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.graph.domain.SugarAcidityGraphZoneShape;
import org.jspecify.annotations.NonNull;


@Schema(description = "Rectangular sugar-acidity graph zone.")
public record RectangleSugarAcidityGraphZoneResponse(
    @Schema(description = "Zone code.", implementation = SugarAcidityZone.class, example = "SUGAR_RICH_APPLES", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull SugarAcidityZone zone,
    @Schema(description = "Specific gravity range for the zone.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull SugarAcidityGraphRangeResponse sugarRange,
    @Schema(description = "Titratable acidity range for the zone.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull SugarAcidityGraphRangeResponse acidityRange
) implements SugarAcidityGraphZoneResponse {

  @Override
  @Schema(description = "Zone shape discriminator.", implementation = SugarAcidityGraphZoneShape.class, example = "RECTANGLE", requiredMode = Schema.RequiredMode.REQUIRED)
  public @NonNull SugarAcidityGraphZoneShape shape() {
    return SugarAcidityGraphZoneShape.RECTANGLE;
  }
}
