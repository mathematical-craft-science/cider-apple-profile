package org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto;

import org.jspecify.annotations.NonNull;


import static java.util.Objects.requireNonNull;

public record RangeMeasurementSeedDto(
    @NonNull MeasurementSeedKind kind,
    @NonNull MeasurementSeedUnit unit,
    @NonNull String min,
    @NonNull String max
) implements MeasurementSeedDto {

  public RangeMeasurementSeedDto {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(unit, "measurement unit cannot be null");
    requireNonNull(min, "range minimum value cannot be null");
    requireNonNull(max, "range maximum value cannot be null");
  }
}
