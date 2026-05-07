package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record StatisticalSugarAcidityAxisPointResponse(
    @NonNull BigDecimal value,
    @NonNull String valueKind,
    @Nullable BigDecimal standardDeviation,
    @Nullable Integer sampleSize
) implements SugarAcidityAxisPointResponse {

  public StatisticalSugarAcidityAxisPointResponse {
    requireNonNull(value, "statistical axis point value cannot be null");
    requireNonNull(valueKind, "statistical axis point value kind cannot be null");

    if (!valueKind.startsWith("STATISTICAL_")) {
      throw new IllegalArgumentException("statistical axis point value kind expected");
    }
  }
}
