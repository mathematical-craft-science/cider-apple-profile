package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record StatisticalChemicalMeasurementRequest(
    @NonNull String kind,
    @NonNull BigDecimal mean,
    @Nullable BigDecimal median,
    @Nullable BigDecimal standardDeviation,
    @Nullable Integer sampleSize
) implements ChemicalMeasurementRequest {

  public StatisticalChemicalMeasurementRequest {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(mean, "statistical mean value cannot be null");
  }
}
