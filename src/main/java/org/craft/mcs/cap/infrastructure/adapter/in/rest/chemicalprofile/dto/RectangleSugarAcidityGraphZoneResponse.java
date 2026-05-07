package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import org.jspecify.annotations.NonNull;


public record RectangleSugarAcidityGraphZoneResponse(
    @NonNull String zone,
    @NonNull SugarAcidityGraphRangeResponse sugarRange,
    @NonNull SugarAcidityGraphRangeResponse acidityRange
) implements SugarAcidityGraphZoneResponse {

  @Override
  public @NonNull String shape() {
    return "RECTANGLE";
  }
}
