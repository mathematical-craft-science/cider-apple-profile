package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public record RangeSugarAcidityAxisPointResponse(
    @NonNull BigDecimal value,
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) implements SugarAcidityAxisPointResponse {

  @Override
  public @NonNull String valueKind() {
    return "RANGE_MIDPOINT";
  }
}
