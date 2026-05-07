package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public record ExactSugarAcidityAxisPointResponse(
    @NonNull BigDecimal value
) implements SugarAcidityAxisPointResponse {

  @Override
  public @NonNull String valueKind() {
    return "EXACT";
  }
}
