package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record RangeChemicalMeasurementRequest(
    @NonNull String kind,
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) implements ChemicalMeasurementRequest {

  public RangeChemicalMeasurementRequest {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(min, "range minimum value cannot be null");
    requireNonNull(max, "range maximum value cannot be null");
  }
}
