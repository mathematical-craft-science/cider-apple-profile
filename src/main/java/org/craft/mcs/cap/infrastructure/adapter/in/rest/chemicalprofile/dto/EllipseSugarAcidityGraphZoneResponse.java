package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public record EllipseSugarAcidityGraphZoneResponse(
    @NonNull String zone,
    @NonNull BigDecimal sugarCenter,
    @NonNull BigDecimal acidityCenter,
    @NonNull BigDecimal sugarRadius,
    @NonNull BigDecimal acidityRadius
) implements SugarAcidityGraphZoneResponse {

  @Override
  public @NonNull String shape() {
    return "ELLIPSE";
  }
}
