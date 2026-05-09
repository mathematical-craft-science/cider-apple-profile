package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ExactChemicalMeasurementRequest(
    @NonNull String kind,
    @NonNull BigDecimal value
) implements ChemicalMeasurementRequest {

  public ExactChemicalMeasurementRequest {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(value, "exact measurement value cannot be null");
  }
}
