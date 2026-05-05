package org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


import static java.util.Objects.requireNonNull;

public record StatisticalMeasurementSeedDto(
    @NonNull MeasurementSeedKind kind,
    @NonNull MeasurementSeedUnit unit,
    @NonNull String mean,
    @Nullable String median,
    @Nullable String standardDeviation,
    @Nullable Integer sampleSize
) implements MeasurementSeedDto {

  public StatisticalMeasurementSeedDto {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(unit, "measurement unit cannot be null");
    requireNonNull(mean, "statistical mean value cannot be null");
  }
}
